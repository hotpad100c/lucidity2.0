#!/usr/bin/env python3
"""
静态校验所有 mixin 的注入目标是否真的存在于当前 classpath 上。

为什么需要这个：mixin 的注入目标（@Mixin 的目标类、@At(target=...) 的方法描述符、
method="..." 选择器）都是字符串，编译器完全看不见。配合 lucidity.mixins.json 里的
"defaultRequire": 0，注入失败时既不报错也不打日志，表现就是"某个功能安静地不工作了"。
升级 Minecraft 或依赖大版本之后，这类失效会成片出现，靠玩游戏一个个撞出来代价很高。

用法（在版本子项目目录下的仓库根运行）：
    python tools/verify_mixin_targets.py

它只检查 stonecutter 当前生效的分支：解析前会先剥掉块注释，而未生效的分支正是被
块注释包起来的。

已知限制：
  - 只检查目标是否存在，不检查注入点在方法体内是否真的能匹配到（比如 @At("INVOKE")
    指向的调用是否还在那个方法里）。目标存在但调用点消失的情况查不出来。
  - 同样查不出"目标还在但语义变了"的情况（例如 1.21.9 把绘制拆成 submit/draw 两段，
    方法还在，但在里面直接画已经无效）。
"""

import glob
import os
import re
import struct
import sys
import zipfile

# ---------------------------------------------------------------- class 文件解析


def parse_class(data):
    """返回 (super_name, [interfaces], [(method_name, descriptor)])。"""
    b = memoryview(data)
    i = 8
    cp_count = struct.unpack_from(">H", b, i)[0]
    i += 2
    cp = {}
    k = 1
    while k < cp_count:
        tag = b[i]
        i += 1
        if tag == 1:
            ln = struct.unpack_from(">H", b, i)[0]
            i += 2
            cp[k] = bytes(b[i:i + ln]).decode("utf-8", "replace")
            i += ln
        elif tag in (7, 8, 16, 19, 20):
            cp[k] = ("ref", struct.unpack_from(">H", b, i)[0])
            i += 2
        elif tag == 15:
            i += 3
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            i += 4
        elif tag in (5, 6):
            i += 8
            k += 1
        else:
            raise ValueError("unknown constant pool tag %d" % tag)
        k += 1

    def name_of(idx):
        v = cp.get(idx)
        if isinstance(v, tuple):
            return cp.get(v[1])
        return v

    i += 2  # access flags
    i += 2  # this class
    super_idx = struct.unpack_from(">H", b, i)[0]
    i += 2
    supername = name_of(super_idx) if super_idx else None
    ic = struct.unpack_from(">H", b, i)[0]
    i += 2
    interfaces = []
    for _ in range(ic):
        interfaces.append(name_of(struct.unpack_from(">H", b, i)[0]))
        i += 2

    methods = []
    for section in range(2):  # fields, then methods
        cnt = struct.unpack_from(">H", b, i)[0]
        i += 2
        for _ in range(cnt):
            i += 2  # access
            nm = cp[struct.unpack_from(">H", b, i)[0]]
            i += 2
            de = cp[struct.unpack_from(">H", b, i)[0]]
            i += 2
            ac = struct.unpack_from(">H", b, i)[0]
            i += 2
            for _a in range(ac):
                i += 2
                ln = struct.unpack_from(">I", b, i)[0]
                i += 4 + ln
            if section == 1:
                methods.append((nm, de))
    return supername, interfaces, methods


# ---------------------------------------------------------------- classpath


def active_version():
    """从 stonecutter.gradle.kts 读当前生效的 Minecraft 版本。"""
    try:
        text = open("stonecutter.gradle.kts", encoding="utf-8").read()
    except OSError:
        return None
    m = re.search(r'stonecutter\s+active\s+"([^"]+)"', text)
    return m.group(1) if m else None


def build_index():
    # 必须锁定当前版本：loom 的缓存里同时躺着历史上构建过的所有 Minecraft，
    # 不过滤的话会拿错版本做对照，得出的结论全是错的。
    version = active_version()
    if not version:
        print("无法从 stonecutter.gradle.kts 读出当前版本", file=sys.stderr)
        sys.exit(2)
    print("对照 Minecraft %s" % version, file=sys.stderr)

    jars = [
        p for p in glob.glob(os.path.expanduser(
            "~/.gradle/caches/fabric-loom/minecraftMaven/**/*.jar"), recursive=True)
        if "sources" not in p and "intermediary" not in p and version in p
    ]
    if not jars:
        print("没找到 Minecraft %s 的 jar" % version, file=sys.stderr)
        sys.exit(2)
    jars += [p for p in glob.glob(".gradle/loom-cache/remapped_mods/**/*.jar", recursive=True)
             if "sources" not in p]
    jars += [p for p in glob.glob(os.path.expanduser(
        "~/.gradle/caches/modules-2/files-2.1/net.fabricmc/**/*.jar"), recursive=True)
        if "sources" not in p and ("fabric-loader" in p or "sponge-mixin" in p)]

    index = {}
    for p in jars:
        try:
            z = zipfile.ZipFile(p)
        except Exception:
            continue
        for n in z.namelist():
            if n.endswith(".class"):
                index.setdefault(n[:-6], (p, n))
    return index


class Classpath:
    def __init__(self, index):
        self.index = index
        self._cache = {}

    def _load(self, cls):
        if cls in self._cache:
            return self._cache[cls]
        entry = self.index.get(cls)
        result = None
        if entry:
            try:
                result = parse_class(zipfile.ZipFile(entry[0]).read(entry[1]))
            except Exception:
                result = None
        self._cache[cls] = result
        return result

    def exists(self, cls):
        return cls in self.index

    def methods(self, cls, _seen=None):
        """含继承链和接口的全部方法。"""
        seen = _seen if _seen is not None else set()
        if cls in seen:
            return []
        seen.add(cls)
        loaded = self._load(cls)
        if loaded is None:
            return []
        supername, interfaces, methods = loaded
        out = list(methods)
        for parent in ([supername] if supername else []) + list(interfaces):
            if parent:
                out.extend(self.methods(parent, seen))
        return out


# ---------------------------------------------------------------- 源码解析

BLOCK_COMMENT = re.compile(r"/\*.*?\*/", re.S)
# 把 "a" + "b" 这种跨行拼接的字符串先合并，否则描述符会被截断
CONCAT = re.compile(r'"\s*\+\s*"')
TARGET = re.compile(r'target\s*=\s*"L([^;"]+);([^("]+)(\([^)"]*\)[^"]*)"')
MIXIN = re.compile(r'@Mixin\s*\(\s*(?:value\s*=\s*)?([A-Za-z_][\w.]*)\.class')
METHOD_SEL = re.compile(r'method\s*=\s*"([^"]+)"')


def scan(cp, roots):
    problems = []
    for path in sorted(set(glob.glob(os.path.join(roots, "**", "*.java"), recursive=True))):
        src = open(path, encoding="utf-8").read()
        if "@Mixin" not in src:
            continue
        active = CONCAT.sub("", BLOCK_COMMENT.sub("", src))
        imports = {
            m.group(1).rsplit(".", 1)[-1]: m.group(1).replace(".", "/")
            for m in re.finditer(r"^import\s+(?:static\s+)?([\w.]+);", active, re.M)
        }
        rel = os.path.relpath(path).replace("\\", "/")

        mixin_match = MIXIN.search(active)
        target_cls = None
        if mixin_match:
            ref = mixin_match.group(1)
            head = ref.split(".")[0]
            if head in imports:
                target_cls = imports[head]
                if "." in ref:  # RenderType.CompositeState -> RenderType$CompositeState
                    target_cls += "$" + ref.split(".", 1)[1].replace(".", "$")
            if target_cls and not cp.exists(target_cls):
                problems.append((rel, "@Mixin 目标类不存在", target_cls, ""))
                target_cls = None

        for m in TARGET.finditer(active):
            owner, name, desc = m.group(1), m.group(2), m.group(3)
            if not cp.exists(owner):
                problems.append((rel, "@At target 的类不存在", owner, ""))
                continue
            ms = cp.methods(owner)
            if (name, desc) not in ms:
                same = sorted({d for n, d in ms if n == name})
                problems.append((rel, "@At target 方法签名不符",
                                 "%s.%s%s" % (owner.rsplit("/", 1)[-1], name, desc),
                                 ("实际: " + "; ".join(same[:3])) if same else "该类没有同名方法"))

        if target_cls:
            ms = cp.methods(target_cls)
            for m in METHOD_SEL.finditer(active):
                sel = m.group(1)
                if "(" in sel:
                    nm, de = sel.split("(", 1)
                    de = "(" + de
                    if (nm, de) not in ms:
                        same = sorted({d for n, d in ms if n == nm})
                        problems.append((rel, "method= 签名不符",
                                         "%s.%s" % (target_cls.rsplit("/", 1)[-1], sel),
                                         ("实际: " + "; ".join(same[:3])) if same else "该类没有同名方法"))
                elif not any(n == sel for n, _ in ms):
                    problems.append((rel, "method= 方法不存在",
                                     "%s.%s" % (target_cls.rsplit("/", 1)[-1], sel),
                                     "该类没有同名方法"))
    return problems


def main():
    index = build_index()
    cp = Classpath(index)
    print("classpath: %d 个类" % len(index), file=sys.stderr)
    problems = scan(cp, os.path.join("src", "main", "java"))
    if not problems:
        print("所有 mixin 注入目标都存在。")
        return 0
    print("\n=== 发现 %d 处失效的注入目标 ===" % len(problems))
    current = None
    for rel, kind, what, extra in problems:
        if rel != current:
            print("\n■ %s" % rel)
            current = rel
        print("   [%s] %s" % (kind, what))
        if extra:
            print("      %s" % extra)
    return 1


if __name__ == "__main__":
    sys.exit(main())
