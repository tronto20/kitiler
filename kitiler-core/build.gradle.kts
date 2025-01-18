plugins {
    buildsrc.convention.`kotlin-jvm`
    buildsrc.convention.`maven-publish`
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    libs.bundles.boms.get().forEach {
        implementation(platform(it))
    }
    libs.bundles.testBoms.get().forEach {
        testImplementation(platform(it))
    }
    implementation(kotlin("reflect"))
    implementation(libs.bundles.logging)
    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxSerializationJson)
    implementation(libs.bundles.jts)
    implementation(libs.gdal)
    implementation(libs.springCore)
    implementation(libs.springExpression)
    implementation(libs.bundles.multik)
    implementation(libs.thymeleaf)

    testImplementation(libs.bundles.test)
}
