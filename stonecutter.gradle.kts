plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.17.19" apply false
    // id("me.modmuss50.mod-publish-plugin") version "1.0.+" apply false
}

stonecutter active "1.21.11"

/*
// Make newer versions be published last
stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
}
 */

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"" + property("mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    constants["release"] = property("mod.id") != "template"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String
    replacements.string{
        direction = eval(current.version, ">=1.21.5")
        replace("fi.dy.masa.malilib.util.Color4f", "fi.dy.masa.malilib.util.data.Color4f")
    }

    // 1.21.11 做了一次大规模包重组，见 https://docs.neoforged.net/primer/docs/1.21.11/
    // 这里只放"纯改名/搬包"的部分；真正的 API 变更（RenderStateShard -> RenderSetup、
    // HitboxesRenderState 移除、sodium 遮挡剔除重构等）用 //? if >=1.21.11 的条件块单独处理。
    // 每个 replacements.string{} 只承载一对替换，所以这里用循环批量声明
    listOf(
        // ResourceLocation 整体改名为 Identifier；包路径没变，裸标识符替换同时修好 import 和用法
        "ResourceLocation" to "Identifier",

        // net.minecraft 顶层工具类下沉
        "net.minecraft.Util" to "net.minecraft.util.Util",

        // RenderType 搬进 rendertype 子包（注意：原来挂在 RenderType 上的静态类型
        // 已经拆到 RenderTypes，那部分不是搬包，得逐处改）
        "net.minecraft.client.renderer.RenderType" to "net.minecraft.client.renderer.rendertype.RenderType",

        // 客户端模型按类型分了子包
        "net.minecraft.client.model.CreeperModel" to "net.minecraft.client.model.monster.creeper.CreeperModel",
        "net.minecraft.client.model.WitherBossModel" to "net.minecraft.client.model.monster.wither.WitherBossModel",

        // 实体按类型分了子包
        "net.minecraft.world.entity.boss.EnderDragonPart" to "net.minecraft.world.entity.boss.enderdragon.EnderDragonPart",
        "net.minecraft.world.entity.projectile.windcharge." to "net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.",
        "net.minecraft.world.entity.projectile.LargeFireball" to "net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball",
        "net.minecraft.world.entity.projectile.WitherSkull" to "net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull",
        "net.minecraft.world.entity.vehicle.AbstractBoat" to "net.minecraft.world.entity.vehicle.boat.AbstractBoat",
        "net.minecraft.world.entity.vehicle.MinecartTNT" to "net.minecraft.world.entity.vehicle.minecart.MinecartTNT",

        // RenderType 上的命名类型全部搬到了 RenderTypes（RenderType 本身退化成不可继承的
        // 具体类）。这里换成整包通配 import：既拿到 RenderTypes，也保证替换是幂等的
        // ——如果写成"再补一行 import"，每次 Refresh 都会再补一次。
        "import net.minecraft.client.renderer.rendertype.RenderType;" to "import net.minecraft.client.renderer.rendertype.*;",
        "RenderType.translucentMovingBlock" to "RenderTypes.translucentMovingBlock",
        "RenderType.entityTranslucent(" to "RenderTypes.entityTranslucent(",
        "RenderType.lines()" to "RenderTypes.lines()",
        "RenderType.debugQuads()" to "RenderTypes.debugQuads()",
        "RenderType.LINES" to "RenderTypes.LINES",

        // ryansrenderingkit 1.2.1 的唯一破坏性改动（这一版是随 1.21.11 一起上的，
        // 所以借用同一个版本判定）
        "model_vertexes" to "modelVertexes",

        // sodium 0.8 把 frapi 子树里的一部分挪了出来。注意只能按类名精确匹配：
        // frapi.render 包还在，NonTerrainBlockRenderContext 仍留在里面，
        // 用包前缀替换会把它一起误伤。
        "net.caffeinemc.mods.sodium.client.render.frapi.helper.ColorHelper" to "net.caffeinemc.mods.sodium.client.render.helper.ColorHelper",
        "net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl" to "net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl",
        "net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext" to "net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext",
        // MutableQuadViewImpl.color(index, argb) 改名成了 setColor
        "quad.color(i," to "quad.setColor(i,",

        // Camera.getPosition() -> position()
        "getMainCamera().getPosition()" to "getMainCamera().position()"
    ).forEach { (old, new) ->
        replacements.string {
            direction = eval(current.version, ">=1.21.11")
            replace(old, new)
        }
    }
}
