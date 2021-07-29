plugins {
    java
    eclipse
    `maven-publish`
    id("fabric-loom") version "0.8-SNAPSHOT"
    id("io.github.juuxel.loom-quiltflower") version "1.1.3"
    //id("net.minecraftforge.gradle") version "5.+"
    //id("com.matthewprenger.cursegradle") version "1.4.0"
    //id("com.modrinth.minotaur") version "1.1.0"
    //id("wtf.gofancy.fancygradle") version "1.0.1"
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(16))

val versionMc: String by project
val versionFLoader: String by project
val versionFAPI: String by project
val versionMajor: String by project
val versionMinor: String by project
val versionPatch: String by project
val versionClassifier: String by project
val versionType: String = versionClassifier.split(".")[0]

val versionCCA: String by project
val versionOmegaConfig: String by project
val versionSomnus: String by project
val versionJEI: String by project
val versionDarkUtils: String by project
val versionCurios: String by project
val versionBookshelf: String by project
val versionRunelic: String by project
val curseForgeId: String by project

version = versionMc + "-" + versionMajor + "." + versionMinor + (if (versionPatch != "0") ".$versionPatch" else "") + if (versionClassifier.isNotEmpty()) "-$versionClassifier" else ""
group = "mods.su5ed"

val versionRaw: String = version.toString().split("-")[1]
val releaseClassifier: String = versionType.ifEmpty { "release" }

/*configure<UserDevExtension> {
    mappings("official", "1.16.5")

    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        val config = Action<RunConfig> {
            properties(mapOf(
                "forge.logging.markers" to "SCAN,REGISTRIES,REGISTRYDUMP,COREMODLOG",
                "forge.logging.console.level" to "debug"
            ))
            workingDirectory = project.file("run").canonicalPath
            source(sourceSets["main"])
        }

        create("client", config)
        create("server", config)
    }
}*/

repositories {
    mavenLocal()
    maven {
        name = "BlameJared"
        url = uri("https://maven.blamejared.com")
    }
    maven {
        name = "Curios"
        url = uri("https://maven.theillusivec4.top")
    }
    maven {
        name = "Progwml6 maven"
        url = uri("https://dvs1.progwml6.com/files/maven/")
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
    mappings(minecraft.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$versionFLoader")

    modImplementation("net.fabricmc.fabric-api:fabric-api:$versionFAPI")

    modImplementation("io.github.onyxstudios.Cardinal-Components-API:cardinal-components-entity:$versionCCA")
    modImplementation("io.github.onyxstudios.Cardinal-Components-API:cardinal-components-base:$versionCCA")
    include("io.github.onyxstudios.Cardinal-Components-API:cardinal-components-entity:$versionCCA")
    include("io.github.onyxstudios.Cardinal-Components-API:cardinal-components-base:$versionCCA")

    modImplementation("draylar.omega-config:omega-config-base:$versionOmegaConfig")
    include("draylar.omega-config:omega-config-base:$versionOmegaConfig:min")

    modImplementation("top.theillusivec4.somnus:somnus-fabric:$versionSomnus")
    include("top.theillusivec4.somnus:somnus-fabric:$versionSomnus")

    modImplementation("curse.maven:comforts-433539:3347978")
    modImplementation("curse.maven:trinkets-341284:3390660")
    //implementation(fg.deobf(group = "mezz.jei", name = "jei-1.16.5", version = versionJEI))
    //compileOnly(fg.deobf(group = "net.darkhax.darkutilities", name = "DarkUtilities-1.16.5", version = versionDarkUtils))
    //compileOnly(fg.deobf(group = "top.theillusivec4.curios", name = "curios-forge", version = versionCurios))
    //compileOnly(fg.deobf(group = "net.darkhax.bookshelf", name = "Bookshelf-1.16.5", version = versionBookshelf))
    //compileOnly(fg.deobf(group = "net.darkhax.runelic", name = "Runelic-1.16.5", version = versionRunelic))
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
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

/*tasks {
    jar {
        finalizedBy("reobfJar")
        manifest {
            attributes(
                "Specification-Title" to "Somnia Awoken",
                "Specification-Vendor" to "Su5eD",
                "Specification-Version" to 1,
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "Su5eD",
                "Implementation-Timestamp" to LocalDateTime.now()
            )
        }
    }
    
    processResources {
        filesMatching("mods.toml") {
            expand("version" to project.version)
        }
    }
    
    register<TaskModrinthUpload>("publishModrinth") {
        token = System.getenv("MODRINTH_TOKEN") ?: project.findProperty("MODRINTH_TOKEN") as String? ?: "DUMMY"
        projectId = "BiSrUr8O"
        versionName = getVersionDisplayName()
        versionNumber = versionRaw
        uploadFile = jar
        addLoader("forge")
        releaseType = releaseClassifier
        changelog = System.getenv("CHANGELOG")
    }
}

curseforge {
    apiKey = System.getenv("CURSEFORGE_TOKEN") ?: project.findProperty("CURSEFORGE_TOKEN") as String? ?: "DUMMY"
    project(closureOf<CurseProject> {
        id = curseForgeId
        changelogType = "markdown"
        changelog = System.getenv("CHANGELOG") ?: ""
        releaseType = releaseClassifier
        mainArtifact(tasks.getByName("jar"), closureOf<CurseArtifact> {
            displayName = getVersionDisplayName()
            relations(closureOf<CurseRelation> {
                optionalDependency("cyclic")
                optionalDependency("comforts")
                optionalDependency("coffee-mod")
                optionalDependency("coffee-spawner")
                optionalDependency("dark-utilities")
                optionalDependency("sleeping-bags")
            })
        })
        addGameVersion("Forge")
        addGameVersion(versionMc)
    })
}*/

fun getVersionDisplayName(): String {
    val name = "Somnia Awoken"
    val classifier: String
    val parts: List<String> = versionClassifier.split(".")
    val classifierName = parts[0]
    var firstLetter = classifierName.substring(0, 1)
    if (classifierName.isNotEmpty()) {
        val remainingLetters = classifierName.substring(1, classifierName.length)
        firstLetter = firstLetter.toUpperCase()
        classifier = firstLetter + remainingLetters + if (parts.size > 1) " ${parts[1]}" else ""
    } else classifier = ""

    return "$name $versionRaw $classifier"
}
