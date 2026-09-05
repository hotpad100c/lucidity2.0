import me.modmuss50.mpp.ReleaseType.STABLE

plugins {
    id("fabric-loom")

    // `maven-publish`
    id("me.modmuss50.mod-publish-plugin")
}

version = "${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = property("mod.id") as String
val requiredJava = when {
    stonecutter.eval(stonecutter.current.version, ">=1.20.6") -> JavaVersion.VERSION_21
    stonecutter.eval(stonecutter.current.version, ">=1.18") -> JavaVersion.VERSION_17
    stonecutter.eval(stonecutter.current.version, ">=1.17") -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    strictMaven("https://maven.terraformersmc.com/releases/", "terraformersmc", "com.terraformersmc")
    strictMaven("https://masa.dy.fi/maven", "masa", "masa.dy.fi")
    strictMaven("https://jitpack.io", "jitpack", "com.github.sakura-ryoko")
    strictMaven( "https://maven.fallenbreath.me/releases","me.fallenbreath" )
    flatDir {
        dirs("libs")
    }
    mavenCentral()
    mavenLocal()//test use
}

val minecraft = stonecutter.current.version
dependencies {

    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    modCompileOnly("com.terraformersmc:modmenu:${property("deps.mod_menu_version")}")
    modImplementation("io.github.hotpad100c:ryansrenderingkit:${property("deps.ryansrenderingkit_version")}")
    //modImplementation("maven.modrinth:sodium:${property("deps.sodium_version")}")
    modCompileOnly("maven.modrinth:sodium:${property("deps.sodium_version")}")
    // 可选兼容：只在编译期需要，运行时由 FabricLoader.isModLoaded 判断
    //modImplementation("maven.modrinth:flashback:${property("deps.flashback_version")}")
    modCompileOnly("maven.modrinth:flashback:${property("deps.flashback_version")}")

    modImplementation("com.github.sakura-ryoko:malilib:${property("deps.malilib_version")}")

    //modCompileOnly(files("pca-protocol-${property("deps.pca_version")}"))
}
val accesswidener = when {
    stonecutter.eval(minecraft, ">=1.21.11") -> "1.21.11.accesswidener"
    stonecutter.eval(minecraft, ">=1.21.9") -> "1.21.9.accesswidener"
    stonecutter.eval(minecraft, ">=1.21.6") -> "1.21.6.accesswidener"
    stonecutter.eval(minecraft, ">=1.21.5") -> "1.21.5.accesswidener"
    stonecutter.eval(minecraft, ">=1.21.3") -> "1.21.4.accesswidener"
    else -> "1.21.1.accesswidener"
}



loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection
    accessWidenerPath = rootProject.file("src/main/resources/accesswideners/$accesswidener")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        runDir = "../../run" // Shares the run directory between versions
    }
}

fabricApi {
    configureDataGeneration() {
        client = true
    }
}
java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks {
    processResources {
        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", project.property("mod.version"))
        inputs.property("minecraft", project.property("mod.mc_dep"))


        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep"),
            "aw_file" to accesswidener,
        )

        filesMatching("fabric.mod.json") {
            expand(props)
        }
        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile }, remapSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

// 发布到 Modrinth，changelog 取自 CHANGELOG.md。
// 只发 Modrinth：模板里的 curseforge 段已删掉。
publishMods {
    file = tasks.remapJar.map { it.archiveFile.get() }
    additionalFiles.from(tasks.remapSourcesJar.map { it.archiveFile.get() })
    displayName = "${property("mod.name")} ${property("mod.version")} for ${property("mod.mc_title")}"
    version = property("mod.version") as String
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")

    // 没设 MODRINTH_TOKEN 就自动空跑：可以先跑一遍看它打算传什么，不会真的上传
    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null

    modrinth {
        projectId = property("publish.modrinth") as String
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        // 每个版本各自的目标 MC 列表，见 versions/<ver>/gradle.properties
        minecraftVersions.addAll(property("mod.mc_targets").toString().split(' '))
        // 运行时必须装的三个。一律用项目 ID 而不是 slug：slug 可以被改名，ID 不会变。
        requires {
            id = "P7dR8mSH" // Fabric API
        }
        requires {
            id = "GcWjdA9I" // malilib
        }
        requires {
            id = "CdJaAf0y" // RyansRenderingKit
        }
        // 可选联动：装了才注册选择性渲染的透明度关键帧，没装也照常跑
        optional {
            id = "4das1Fjq" // Flashback
        }
    }
}
/*
// Publishes builds to a maven repository under `com.example:template:0.1.0+mc`
publishing {
    repositories {
        maven("https://maven.example.com/releases") {
            name = "myMaven"
            // To authenticate, create `myMavenUsername` and `myMavenPassword` properties in your Gradle home properties.
            // See https://stonecutter.kikugie.dev/wiki/tips/properties#defining-properties
            credentials(PasswordCredentials::class.java)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "${property("mod.group")}.${property("mod.id")}"
            artifactId = property("mod.id") as String
            version = project.version

            from(components["java"])
        }
    }
}
 */

// 移植期间临时放开 javac 的错误上限，便于一次看到全部问题
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "2000"))
}
