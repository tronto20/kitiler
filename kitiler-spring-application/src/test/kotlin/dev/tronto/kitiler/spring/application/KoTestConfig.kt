package dev.tronto.kitiler.spring.application

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.junit5.JUnitExtensionAdapter
import io.kotest.extensions.spring.SpringExtension
import io.mockk.junit5.MockKExtension

object KoTestConfig : AbstractProjectConfig() {

    override fun extensions(): List<Extension> = listOf(SpringExtension, JUnitExtensionAdapter(MockKExtension()))
}
