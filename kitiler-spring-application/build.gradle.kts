import dev.tronto.kitiler.buildsrc.tasks.PathExec
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.springframework.boot.buildpack.platform.build.PullPolicy
import org.springframework.boot.gradle.tasks.bundling.BootArchive
import org.springframework.boot.gradle.tasks.bundling.DockerSpec.DockerRegistrySpec

plugins {
    buildsrc.convention.`kotlin-jvm`
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.graalvm) apply false
    alias(libs.plugins.restdocsApiSpec)
}

val jvmVersion = (properties["jvm.version"] as? String)?.toIntOrNull() ?: 17

if (properties["image.debug.enabled"].toString().toBoolean()) {
    afterEvaluate {
        tasks.bootBuildImage {
            val envs = environment.get().toMutableMap()
            val javaOps = envs["BPE_APPEND_JAVA_TOOL_OPTIONS"]?.split(' ') ?: emptyList()

            val debugPort = properties["image.debug.port"]?.toString()?.toIntOrNull() ?: 5005
            val suspend = properties["image.debug.suspend"]?.toString()?.toBoolean() ?: false
            val suspendString = if (suspend) "y" else "n"

            envs["BPE_DELIM_JAVA_TOOL_OPTIONS"] = " "
            envs["BPE_APPEND_JAVA_TOOL_OPTIONS"] =
                (javaOps + "-agentlib:jdwp=transport=dt_socket,server=y,suspend=$suspendString,address=*:$debugPort")
                    .joinToString(separator = " ")
            environment.set(envs)
        }
    }
}

dependencies {
    libs.bundles.boms.get().forEach {
        implementation(platform(it))
    }
    libs.bundles.testBoms.get().forEach {
        testImplementation(platform(it))
    }
    implementation(libs.bundles.logging)
    implementation(projects.springBootKitilerStarterCore)
    implementation(libs.swaggerParser)
    implementation(libs.springBootWebflux)

    testImplementation(libs.bundles.test)
    testImplementation(libs.bundles.springTest)
    testImplementation(libs.bundles.jts)

    testImplementation(libs.springRestdocsWebTestClient)
    testImplementation(libs.restdocsApiSpec)
    testImplementation(libs.restdocsApiSpecWebTestClient)
}

val runImageName = "docker.io/library/kitiler-spring-application-runner:latest"
val buildRunnerImageTask = tasks.register("buildRunnerImage", PathExec::class.java) {
    this.group = "build"
    workingDir = project.projectDir
    executable = "docker"
    setArgs(
        listOf(
            "build",
            ".",
            "-t",
            runImageName,
        )
    )

    val gdalVersion = libs.versions.gdal.get()
    args("--build-arg")
    args("GDAL_VERSION=${gdalVersion}")
    if (DefaultNativePlatform.getCurrentArchitecture().isArm64) {
        args("--build-arg")
        args("JNI=/usr/lib/aarch64-linux-gnu/jni")
    }
}


if (properties["image.native.enabled"].toString().toBoolean()) {
    apply {
        this.plugin(libs.plugins.graalvm.get().pluginId)
    }
    afterEvaluate {
        tasks.bootBuildImage {
            val customEnvs = mutableMapOf<String, String>()
            (properties["image.native.compression"] as? String)?.let { compression ->
                customEnvs["BP_BINARY_COMPRESSION_METHOD"] = compression
            }

            val taskEnv = (this.environment.orNull ?: emptyMap())
            this.environment.set(taskEnv + customEnvs)
        }
    }
}

tasks.bootBuildImage {
    dependsOn(buildRunnerImageTask)
    this.runImage.set(runImageName)
    this.pullPolicy.set(PullPolicy.IF_NOT_PRESENT)

    fun DockerRegistrySpec.configure(name: String) {
        properties["image.registry.$name.url"]?.let { url.set(it as String) }
        properties["image.registry.$name.username"]?.let { username.set(it as String) }
        properties["image.registry.$name.password"]?.let { password.set(it as String) }
        properties["image.registry.$name.email"]?.let { email.set(it as String) }
        properties["image.registry.$name.token"]?.let { token.set(it as String) }
    }

    docker {
        publishRegistry.configure("publish")
        builderRegistry.configure("builder")
    }

    val baseName = (properties["image.name"] as? String?)?.trimEnd('/') ?: "docker.io/kitiler"
    val tags = (properties["image.tags"] as? String?)
        ?.split(',')
        ?.map { it.trim() }
        ?.ifEmpty { null }
        ?: emptyList()

    this.tags.set((tags).map { "$baseName:${it.replace("{VERSION}", version.toString(), true)}" })
    this.imageName.set(this.tags.get().firstOrNull() ?: "$baseName:latest")

    (properties["image.push"] as? String?)?.let { publish.set(it.toBoolean()) }
    val envs = environment.get().toMutableMap()
    envs["BP_JVM_VERSION"] = jvmVersion.toString()
    envs["BPE_DELIM_JAVA_TOOL_OPTIONS"] = " "
    environment.set(envs)
}


if (DefaultNativePlatform.getCurrentArchitecture().isArm64) {
    buildRunnerImageTask {
        args("--build-arg")
        args("STACK_ID=io.buildpacks.stacks.jammy")
    }

    tasks.bootBuildImage {
        builder.set("paketobuildpacks/builder-jammy-base:latest")
    }
}

tasks.register("buildImage") {
    group = "build"
    dependsOn(tasks.bootBuildImage)
}

val generatedResourceDir = layout.buildDirectory.dir("generated").map { it.dir("generatedResources") }
val generatedSourceSet = sourceSets.create("generated") {
    resources.srcDir(generatedResourceDir)
}

val createGeneratedResourceDirTask = tasks.create("createGeneratedResourceDir") {
    this.actions = listOf(Action {
        generatedResourceDir.get().asFile.mkdirs()
    })
}

val disableLintSourceSets = listOf("aot", "aotTest", generatedSourceSet.name)

afterEvaluate {
    val disableFormatTasks = disableLintSourceSets.mapNotNull {
        val sourceSet = kotlin.sourceSets.findByName(it) ?: return@mapNotNull null
        tasks.findByName("formatKotlin${sourceSet.name.replaceFirstChar(Char::titlecase)}")
    }
    val disableLintTasks = disableLintSourceSets.mapNotNull {
        val sourceSet = kotlin.sourceSets.findByName(it) ?: return@mapNotNull null
        tasks.findByName("lintKotlin${sourceSet.name.replaceFirstChar(Char::titlecase)}")
    }
    tasks.formatKotlin {
        setDependsOn(
            dependsOn.filterIsInstance<Provider<Task>>()
                .filter { it.get().name !in disableFormatTasks.map { it.name } })
    }
    tasks.lintKotlin {
        setDependsOn(
            dependsOn.filterIsInstance<Provider<Task>>().filter { it.get().name !in disableLintTasks.map { it.name } })
    }
}

val buildDocsTask = tasks.create("buildDocs") {
    dependsOn("openapi3")
    group = "documentation"
}

afterEvaluate {
    tasks.getByName("openapi3") {
        dependsOn(createGeneratedResourceDirTask)
    }
}

openapi3 {
    title = "kitiler"
    description = "dynamic tile server."
    outputDirectory = generatedResourceDir.get().asFile.resolve("swagger").absolutePath
}

tasks.getByName(generatedSourceSet.processResourcesTaskName) {
    dependsOn(buildDocsTask)
}

tasks.withType<BootArchive>() {
    dependsOn(generatedSourceSet.classesTaskName)
    this.classpath(generatedSourceSet.runtimeClasspath)
}
