dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

pluginManagement {
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

include(":kitiler-core")
include(":spring-boot-kitiler-autoconfigure")
include(":spring-boot-kitiler-starter-core")
include(":kitiler-spring-application")
