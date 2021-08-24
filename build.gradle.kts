plugins {
    java
    `maven-publish`
    id("fabric-loom") version "0.9-SNAPSHOT"
    id("io.github.juuxel.loom-quiltflower") version "1.3.0"
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(16))

val versionMc: String by project
val versionFLoader: String by project
val versionFAPI: String by project
val versionMajor: String by project
val versionMinor: String by project
val versionPatch: String by project
val versionClassifier: String by project

val versionCCA: String by project
val versionOmegaConfig: String by project
val versionSomnus: String by project

version = versionMajor + "." + versionMinor + (if (versionPatch != "0") ".$versionPatch" else "") + (if (versionClassifier.isNotEmpty()) "-$versionClassifier" else "") + "+fabric-$versionMc"
group = "io.github.alkyaly"

repositories {
    mavenLocal()
    maven {
        name = "Somnus"
        url = uri("https://maven.theillusivec4.top")
    }
    maven {
        name = "Ladysnake Mods"
        url = uri("https://ladysnake.jfrog.io/artifactory/mods")
    }
    maven {
        name = "Curse Maven"
        url = uri("https://www.cursemaven.com")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$versionMc")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$versionFLoader")

    modImplementation("net.fabricmc.fabric-api:fabric-api:$versionFAPI")

    modImplementation("io.github.onyxstudios.Cardinal-Components-API:cardinal-components-entity:$versionCCA")
    modImplementation("io.github.onyxstudios.Cardinal-Components-API:cardinal-components-base:$versionCCA")

    modImplementation("draylar.omega-config:omega-config-base:$versionOmegaConfig")
    include("draylar.omega-config:omega-config-base:$versionOmegaConfig:min")

    //todo: soon this will not be needed https://github.com/FabricMC/fabric/pull/1633
    modImplementation("top.theillusivec4.somnus:somnus-fabric:$versionSomnus")
    include("top.theillusivec4.somnus:somnus-fabric:$versionSomnus")

    modRuntime("curse.maven:comforts-433539:3347978")
}

tasks.processResources {
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "UTF-8"
    options.release.set(16)
}

java {
    withSourcesJar()
}

tasks.jar {
    from("LICENSE")
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            artifact(tasks.remapJar) {
                builtBy(tasks.remapJar)
            }
            artifact(tasks.getByName("sourcesJar")) {
                builtBy(tasks.remapSourcesJar)
            }
        }
    }
}