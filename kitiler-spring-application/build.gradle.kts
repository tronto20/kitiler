import dev.tronto.kitiler.buildsrc.tasks.PathExec
import org.springframework.boot.buildpack.platform.build.PullPolicy
import org.springframework.boot.gradle.tasks.bundling.BootArchive
import org.springframework.boot.gradle.tasks.bundling.DockerSpec.DockerRegistrySpec

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.serialization")
    id("org.springframework.boot")
    id("org.graalvm.buildtools.native") apply false
    id("org.jmailen.kotlinter")
    id("com.epages.restdocs-api-spec")
}

val jvmVersion = (properties["jvm.version"] as? String)?.toIntOrNull() ?: 21

if (properties["image.native.enabled"].toString().toBoolean()) {
    apply(plugin = "org.graalvm.buildtools.native")
    afterEvaluate {
        tasks.bootBuildImage {
            val customEnvs = mutableMapOf<String, String>()
            (properties["image.native.compression"] as? String)?.let { compression ->
                customEnvs["BP_BINARY_COMPRESSION_METHOD"] = compression
            }

            val taskEnv = (this.environment.orNull ?: emptyMap())
            this.environment.set(taskEnv + customEnvs)
        }
        tasks.withType<Exec>().findByName("buildRunnerImage")?.apply {
            this.args("--build-arg")
            this.args("STACK_ID=io.buildpacks.stacks.jammy.tiny")
        }
    }

}

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

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(projects.kitilerDependencies))
    implementation("io.github.oshai:kotlin-logging-jvm")
    implementation("io.swagger.parser.v3:swagger-parser")
    implementation(projects.springBootKitilerStarterCore)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("org.locationtech.jts:jts-core")
    testImplementation("io.kotest:kotest-runner-junit5")
    testImplementation("io.kotest:kotest-extensions-junit5")
    testImplementation("io.kotest:kotest-assertions-core")
    testImplementation("io.kotest.extensions:kotest-extensions-spring")
    testImplementation("io.mockk:mockk")
    testImplementation("org.springframework.restdocs:spring-restdocs-webtestclient")
    testImplementation("com.ninja-squad:springmockk")
    testImplementation("com.epages:restdocs-api-spec")
    testImplementation("com.epages:restdocs-api-spec-webtestclient")
}

val buildRunnerImageTask = tasks.register("buildRunnerImage", PathExec::class.java) {
    this.group = "build"
    workingDir = project.projectDir
    executable = "docker"
    setArgs(
        listOf(
            "build",
            ".",
            "-t",
            "docker.io/library/spring-application-runner:latest",
        )
    )

    val gdalVersion = properties["gdal.version"]
    args("--build-arg")
    args("GDAL_VERSION=${gdalVersion}")
}

tasks.bootBuildImage {
    dependsOn(buildRunnerImageTask)
    this.runImage.set("docker.io/library/spring-application-runner:latest")
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
    environment.set(envs)
}

kotlin {
    jvmToolchain(jvmVersion)
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

tasks.test {
    useJUnitPlatform()
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
