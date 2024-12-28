import java.io.FileInputStream
import java.util.Properties

plugins {
    `kotlin-dsl`
}

val properties = Properties().apply {
    FileInputStream(file("../gradle.properties")).use {
        load(it)
    }
}
repositories {
    mavenCentral()
}

val jvmVersion = (properties["jvm.version"] as? String)?.toIntOrNull() ?: 21
kotlin {
    jvmToolchain(jvmVersion)
}
