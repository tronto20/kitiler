package buildsrc.convention

import com.vanniktech.maven.publish.SonatypeHost

plugins {
    com.vanniktech.maven.publish
}

mavenPublishing {
    beforeEvaluate {
        @Suppress("UnstableApiUsage")
        pomFromGradleProperties()
    }
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}
