group = "me.mrfunny.minigame"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

val include: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    include("net.minestom:minestom-snapshots:0d47d97417") {
        exclude("org.slf4j")
    }
    include("ch.qos.logback:logback-classic:1.5.16")
    include("org.fusesource.jansi:jansi:2.4.1")
    include("dev.hollowcube:schem:1.3.1")
    include("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")
    include("com.github.semenishchev:MinestomPvP:fe6682f8bb")
    include(project(":api")) {
        exclude("net.kyori", "adventure-api")
    }
    include(project(":assets"))
}

tasks.withType<Jar> {
    include.forEach { dep ->
        from(project.zipTree(dep)){
            exclude("META-INF', 'META-INF/**")
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }
    manifest {
        manifest.attributes["Main-Class"] = "me.mrfunny.minigame.minestom.Main"
    }
}

tasks.test {
    useJUnitPlatform()
}