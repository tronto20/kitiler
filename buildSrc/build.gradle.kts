import java.io.FileInputStream
import java.util.Properties

plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "5.1.2"
}

val properties = Properties().apply {
    FileInputStream(file("../gradle.properties")).use {
        load(it)
    }
}

val jvmVersion = (properties["jvm.version"] as? String)?.toIntOrNull() ?: 21
kotlin {
    jvmToolchain(jvmVersion)
}

dependencies {
    implementation(libs.kotlinGradlePlugin)
    implementation(libs.kotlinterGradlePlugin)
    implementation(libs.mavenPublishGradlePlugin)
}
