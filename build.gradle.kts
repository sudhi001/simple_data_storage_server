plugins {
    kotlin("jvm") version "1.9.23"
    id("io.ktor.plugin") version "2.3.12"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "in.sudhi.lib"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Ktor core dependencies
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")

    // Common Ktor features
    implementation("io.ktor:ktor-server-host-common:2.3.12")
    implementation("io.ktor:ktor-server-cors:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.6")
}

kotlin {
    jvmToolchain(17)
}

application {
    // Set the main class for the application
    mainClass.set("in.sudhi.lib.MainKt")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    // Ensure that the shadow JAR includes the main class in the manifest
    manifest {
        attributes["Main-Class"] = "in.sudhi.lib.MainKt"
    }
}
