import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    application
}

group = "dev.jombi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.dv8tion:JDA:5.0.0-beta.16") {
        exclude(module = "opus-java")
    }
    implementation("com.github.minndevelopment:jda-ktx:0.10.0-beta.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("ch.qos.logback:logback-classic:1.3.11")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    compilerOptions {
        jvmToolchain(11)
        languageVersion.set(KotlinVersion.KOTLIN_1_9)
        apiVersion.set(KotlinVersion.KOTLIN_1_9)
    }
}

application {
    mainClass.set("dev.jombi.woorole.MainKt")
}