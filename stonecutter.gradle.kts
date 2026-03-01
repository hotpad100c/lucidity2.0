plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.14.10" apply false
    // id("me.modmuss50.mod-publish-plugin") version "1.0.+" apply false
}

stonecutter active "1.21.4"

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
}
