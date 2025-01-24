plugins {
    buildsrc.convention.`kotlin-jvm`
    buildsrc.convention.`maven-publish`
//    kotlin("plugin.spring")
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.restdocsApiSpec)
}

dependencies {
    libs.bundles.boms.get().forEach {
        implementation(platform(it))
    }
    implementation(kotlin("reflect"))
    compileOnly(projects.kitilerCore)
    compileOnly(libs.thymeleaf)
    compileOnly(libs.springWebflux)
    compileOnly(libs.kotlinxSerializationJson)
    implementation(libs.springBootAutoConfigure)
    implementation(libs.kotlinxCoroutinesCore)
}

