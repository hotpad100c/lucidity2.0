#!/usr/bin/env python3
"""
把 stonecutter 的版本条件注释从源码里剥掉，只留下当前生效的那一份代码。

这个分支只面向 1.21.11，不再往下兼容，那些条件块就只剩噪音了。

原理
----
stonecutter 处理过的源码里，生效的分支是裸代码，未生效的分支被整段包在块注释里：

    //? if >=1.21.9 {
    生效代码
    //?} else {
    /*未生效代码
    *///?}

所以：标记行一律删掉；标记行之后如果紧跟着一个块注释，那整块就是未生效分支，删掉。

嵌套是这里唯一的坑。未生效分支内部可能还有条件块，那些内层的 `*/` 会被 stonecutter
转义成 `^/`，正是靠这一点才能安全地找到外层注释真正的结尾——只认行首的 `*/`，
不会被内层的 `^/` 提前截断。第一版用正则找标记，把内层的 `//?` 当成了顶层标记，
分段直接错位，改坏了文件。

用法
----
    python tools/strip_stonecutter.py --check   # 只报告
    python tools/strip_stonecutter.py           # 就地改写 src/main/java

改完必须比对字节码：javap -c 不输出行号表，所以变换如果等价，前后反汇编应完全一致。
"""

import glob
import re
import sys

# 内联标记：/*? … */ 到第一个 */ 为止
INLINE_MARKER = re.compile(r"/\*\?[^*]*(?:\*(?!/)[^*]*)*\*/")
# swap：/*$ name*/
SWAP = re.compile(r"/\*\$[^*]*(?:\*(?!/)[^*]*)*\*/[ \t]*")
# 整段就是一个块注释
ONLY_BLOCK_COMMENT = re.compile(r"^\s*/\*[^*]*(?:\*(?!/)[^*]*)*\*/\s*$", re.S)


def is_marker(stripped):
    return stripped.startswith("//?")


def opens_inactive(stripped):
    """行首的块注释开头，且不是内联标记 / swap —— 说明这里开始一段未生效分支。"""
    return stripped.startswith("/*") and not stripped.startswith(("/*?", "/*$"))


def closes_inactive(stripped):
    """收尾行同时又是标记（*///?}）。主循环靠它把这种行当标记处理。"""
    if not stripped.startswith("*/"):
        return False
    return stripped[2:].lstrip().startswith("//?")


def ends_inactive(stripped):
    """未生效分支的结束。收尾的 */ 可能单独占一行，标记在下一行，
    也可能和标记同行写成 *///?}。内层的 */ 被转义成 ^/，所以只认行首的 */ 是安全的。"""
    return stripped.startswith("*/")


def strip_lines(text):
    lines = text.splitlines(keepends=True)
    out = []
    i = 0
    n = len(lines)
    while i < n:
        stripped = lines[i].lstrip()
        # 收尾行（*///?}）本身也是标记，交给同一个分支处理：
        # 一段未生效分支的收尾可能又是 else，后面紧跟着另一段未生效分支，
        # 所以吃完一段之后必须回到这里重新判断，不能就地把收尾行消费掉。
        if is_marker(stripped) or closes_inactive(stripped):
            i += 1
            # 标记和块注释之间可能夹着空行，要跳过之后再判断，
            # 否则这一段未生效分支会被当成正常代码留下来——它内部往往还有嵌套标记，
            # 那些标记接着会被误判成顶层的，整个文件就错位了。
            j = i
            while j < n and not lines[j].strip():
                j += 1
            if j < n and opens_inactive(lines[j].lstrip()):
                i = j
                while i < n and not ends_inactive(lines[i].lstrip()):
                    i += 1
                # 收尾行如果本身就是标记（*///?}），留给主循环处理；
                # 如果只是个光秃秃的 */，它属于这段未生效分支，就地吃掉
                if i < n and not closes_inactive(lines[i].lstrip()):
                    i += 1
            continue
        out.append(lines[i])
        i += 1
    return "".join(out)


def strip_inline(text):
    """内联链：标记之间整段是块注释的删掉，标记本身删掉。"""
    while True:
        markers = list(INLINE_MARKER.finditer(text))
        if not markers:
            return text
        for a, b in zip(markers, markers[1:]):
            seg = text[a.end():b.start()]
            if seg and ONLY_BLOCK_COMMENT.match(seg):
                text = text[:a.end()] + text[b.start():]
                break
        else:
            return INLINE_MARKER.sub("", text)


def process(text):
    return SWAP.sub("", strip_inline(strip_lines(text)))


def main():
    check_only = "--check" in sys.argv
    changed = 0
    leftovers = []
    for path in sorted(glob.glob("src/main/java/**/*.java", recursive=True)):
        src = open(path, encoding="utf-8").read()
        if not any(m in src for m in ("//?", "/*?", "/*$")):
            continue
        out = process(src)
        if out != src:
            changed += 1
            if not check_only:
                with open(path, "w", encoding="utf-8", newline="\n") as fh:
                    fh.write(out)
        for marker in ("//?", "/*?", "/*$", "/^", "^/"):
            if marker in out:
                leftovers.append((path.replace("\\", "/"), marker))
    print("%s %d 个文件" % ("将改写" if check_only else "已改写", changed))
    if leftovers:
        print("\n仍有残留标记，需要人工检查：")
        for path, marker in sorted(set(leftovers)):
            print("   %-70s <- %s" % (path, marker))
        return 1
    print("没有残留标记。")
    return 0


if __name__ == "__main__":
    sys.exit(main())
