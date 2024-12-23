import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jmailen.kotlinter")
    id("com.vanniktech.maven.publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(projects.kitilerDependencies))
    implementation(kotlin("reflect"))
    implementation("io.github.oshai:kotlin-logging-jvm")
    implementation("org.slf4j:slf4j-api")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.locationtech.jts:jts-core")
    implementation("org.gdal:gdal")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.springframework:spring-core")
    implementation("org.springframework:spring-expression")

    implementation("org.jetbrains.kotlinx:multik-core")
    implementation("org.jetbrains.kotlinx:multik-default")

    implementation("org.thymeleaf:thymeleaf")
}

val jvmVersion = (properties["jvm.version"] as? String)?.toIntOrNull() ?: 21
kotlin {
    jvmToolchain(jvmVersion)
}

mavenPublishing {
    beforeEvaluate {
        @Suppress("UnstableApiUsage")
        pomFromGradleProperties()
    }
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}
