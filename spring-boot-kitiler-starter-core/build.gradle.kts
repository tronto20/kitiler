plugins {
    buildsrc.convention.`kotlin-jvm`
    buildsrc.convention.`maven-publish`
}

dependencies {
    libs.bundles.boms.get().forEach {
        implementation(platform(it))
    }
    api(kotlin("reflect"))
    api(projects.kitilerCore)
    api(projects.springBootKitilerAutoconfigure)
    api(libs.thymeleaf)
    api(libs.kotlinxCoroutinesReactor)
    api(libs.kotlinxSerializationJson)
}

