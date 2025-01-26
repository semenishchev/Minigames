plugins {
    id("java")
}

group = "me.mrfunny.minigame"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.kyori:adventure-api:4.18.0")
}