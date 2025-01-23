group = "me.mrfunny.minigame"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.minestom:minestom-snapshots:0d47d97417")
    implementation(project(":api"))
}

tasks.test {
    useJUnitPlatform()
}