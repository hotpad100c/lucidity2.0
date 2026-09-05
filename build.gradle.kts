import me.modmuss50.mpp.ReleaseType.STABLE

plugins {
    id("fabric-loom")

    // `maven-publish`
    id("me.modmuss50.mod-publish-plugin")
}

val modVersion = "${property("mod.version")}+${stonecutter.current.version}"

version = modVersion
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

    modImplementation("com.github.sakura-ryoko:malilib:${property("deps.malilib_version")}")

    //modCompileOnly(files("pca-protocol-${property("deps.pca_version")}"))
}
val accesswidener = when {
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
        inputs.property("version", modVersion)
        inputs.property("minecraft", project.property("mod.mc_dep"))

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to modVersion,
            "minecraft" to project.property("mod.mc_dep"),
            "aw_file" to accesswidener
        )

        filesMatching("fabric.mod.json") { expand(props) }

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

val modrinthToken = providers.environmentVariable("MODRINTH_TOKEN")
    .orElse(providers.gradleProperty("modrinthToken"))

publishMods {
    file = tasks.remapJar.map { it.archiveFile.get() }
    additionalFiles.from(tasks.remapSourcesJar.map { it.archiveFile.get() })
    displayName = "${property("mod.name")} ${property("mod.version")} for ${property("mod.mc_title")}"
    version = modVersion
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")

    dryRun = !modrinthToken.isPresent

    modrinth {
        projectId = property("publish.modrinth") as String
        accessToken = modrinthToken
        minecraftVersions.addAll(property("mod.mc_targets").toString().split(' '))
        requires {
            id = "P7dR8mSH" // Fabric API
        }
        requires {
            id = "GcWjdA9I" // malilib
        }
        requires {
            id = "CdJaAf0y" // RyansRenderingKit
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
