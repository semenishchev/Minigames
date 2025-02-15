group = "me.mrfunny.minigame"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.1")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("net.kyori:adventure-api:4.18.0")
    implementation("redis.clients:jedis:5.2.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
}