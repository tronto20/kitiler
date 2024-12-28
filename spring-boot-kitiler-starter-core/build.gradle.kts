import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    id("org.jmailen.kotlinter")
    id("com.vanniktech.maven.publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(projects.kitilerDependencies))
    api(kotlin("reflect"))
    api(projects.kitilerCore)
    api(projects.springBootKitilerAutoconfigure)
    api("org.thymeleaf:thymeleaf")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json")
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
