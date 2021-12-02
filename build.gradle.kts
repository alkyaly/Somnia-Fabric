plugins {
    java
    `maven-publish`
    id("fabric-loom") version "0.10-SNAPSHOT"
    id("io.github.juuxel.loom-quiltflower-mini") version "1.1.0"
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

val versionMc: String by project
val versionFLoader: String by project
val versionFAPI: String by project
val versionMajor: String by project
val versionMinor: String by project
val versionPatch: String by project
val versionClassifier: String by project

val versionCCA: String by project
val versionOmegaConfig: String by project

version = versionMajor + "." + versionMinor + (if (versionPatch != "0") ".$versionPatch" else "") + (if (versionClassifier.isNotEmpty()) "-$versionClassifier" else "") + "+fabric-$versionMc"
group = "io.github.alkyaly"

repositories {
    mavenLocal()
    maven {
        name = "Ladysnake Mods"
        url = uri("https://ladysnake.jfrog.io/artifactory/mods")
    }
    maven {
        name = "Curse Maven"
        url = uri("https://www.cursemaven.com")
    }
    maven {
        name = "Parchment"
        url = uri("https://maven.parchmentmc.org")
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
    include("draylar.omega-config:omega-config-base:$versionOmegaConfig")
}

tasks.processResources {
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
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