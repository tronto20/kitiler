package buildsrc.convention

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jmailen.gradle.kotlinter.tasks.InstallPreCommitHookTask
import org.jmailen.gradle.kotlinter.tasks.InstallPrePushHookTask

plugins {
    kotlin("jvm")
    org.jmailen.kotlinter
}

group = "dev.tronto"
ext["GROUP"] = group
ext["VERSION_NAME"] = version.toString()

val jvmVersion = (properties["jvm.version"] as? String)?.toIntOrNull() ?: 21
kotlin {
    jvmToolchain(jvmVersion)
}

dependencies {
    implementation(kotlin("reflect"))
}

tasks.withType<Test>().configureEach {
    // Configure all test Gradle tasks to use JUnitPlatform.
    useJUnitPlatform()

    // Log information about all test results, not only the failed ones.
    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED
        )
    }
}


if (!rootProject.extra.has("buildsrc.internal.install-git-hooks")) {
    rootProject.extra.set("buildsrc.internal.install-git-hooks", true)

    val preCommit by project.rootProject.tasks.creating(InstallPreCommitHookTask::class) {
        group = "build setup"
        description = "Installs Kotlinter Git pre-commit hook"
    }

    val prePush by project.rootProject.tasks.creating(InstallPrePushHookTask::class) {
        group = "build setup"
        description = "Installs Kotlinter Git pre-push hook"
    }

    project.rootProject.tasks.getByName("prepareKotlinBuildScriptModel") {
        dependsOn(preCommit, prePush)
    }
}


