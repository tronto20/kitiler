import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.jmailen.kotlinter")
    id("com.vanniktech.maven.publish")
    id("com.epages.restdocs-api-spec")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(projects.kitilerDependencies))
    implementation(kotlin("reflect"))
    compileOnly(projects.kitilerCore)
    compileOnly("org.thymeleaf:thymeleaf")
    compileOnly("org.springframework:spring-webflux")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
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
