pluginManagement {
    val kotlinVersion = extra["kotlin.version"].toString()
    val kotlinterVersion = extra["kotlinter.version"].toString()
    val springBootVersion = extra["spring.boot.version"].toString()
    val graalvmNativeVersion = extra["graalvm.native.version"].toString()
    val springRestDocsApiSpecVersion = extra["spring.restdocs-api-spec.version"].toString()
    val mavenPublishVersion = extra["publish.maven-central.plugin.version"].toString()

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("org.jmailen.kotlinter") version kotlinterVersion
        id("org.springframework.boot") version springBootVersion
        id("org.graalvm.buildtools.native") version graalvmNativeVersion
        id("com.epages.restdocs-api-spec") version springRestDocsApiSpecVersion
        id("com.vanniktech.maven.publish") version mavenPublishVersion
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "kitiler"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":kitiler-dependencies")
include(":kitiler-core")
include(":spring-boot-kitiler-autoconfigure")
include(":spring-boot-kitiler-starter-core")
include(":kitiler-spring-application")
