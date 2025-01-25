pluginManagement {
    repositories {
        maven {
            url = uri("https://raw.githubusercontent.com/graalvm/native-build-tools/snapshots")
        }
        gradlePluginPortal()
    }
}



rootProject.name = "MinigameServer"
include("minestom")
include("api")
