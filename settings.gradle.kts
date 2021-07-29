pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven {
            url = uri("https://maven.fabricmc.net")
        }
        maven {
            url = uri("https://server.bbkr.space/artifactory/libs-release/")
        }
    }
}

rootProject.name = "Somnia-Awoken"
