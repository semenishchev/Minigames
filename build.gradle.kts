plugins {
    id("java")
}

group = "me.mrfunny.minigame"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val targetJavaVersion = 21
subprojects {
    apply {
        plugin("java")
    }
    java {
        val javaVersion = JavaVersion.toVersion(targetJavaVersion);
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        if (JavaVersion.current() < javaVersion) {
            toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
        }
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }
}