import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
}

group = "me.denny"
version = "1.0-SNAPSHOT"

val ktorVersion = "1.6.8"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        name = "ktor-eap"
    }
}

dependencies {
    implementation("io.ktor:ktor-client-core-jvm:2.0.0-eap-256")
    implementation("io.ktor:ktor-client-logging-jvm:2.0.0-eap-256")
    implementation("io.ktor:ktor-client-serialization-jvm:2.0.0-eap-256")
    implementation("io.ktor:ktor-client-cio-jvm:2.0.0-eap-256")
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}