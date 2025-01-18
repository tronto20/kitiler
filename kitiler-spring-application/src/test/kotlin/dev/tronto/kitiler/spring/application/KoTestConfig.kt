package dev.tronto.kitiler.spring.application

import dev.tronto.kitiler.core.outgoing.adaptor.gdal.SpatialReferenceCRSFactory
import dev.tronto.kitiler.core.outgoing.port.CRS
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.core.listeners.AfterSpecListener
import io.kotest.core.listeners.BeforeSpecListener
import io.kotest.core.spec.Spec
import io.kotest.extensions.junit5.JUnitExtensionAdapter
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkObject
import java.net.URI

object KoTestConfig : AbstractProjectConfig() {
    private class MockkGdalExtension :
        AfterSpecListener,
        BeforeSpecListener {
        override suspend fun beforeSpec(spec: Spec) {
            mockkObject(SpatialReferenceCRSFactory)
            every { SpatialReferenceCRSFactory.create(any()) } answers {
                val crsString = this.arg<String>(0)
                object : CRS {
                    override val name: String
                        get() = TODO("Not yet implemented")
                    override val wkt: String
                        get() = TODO("Not yet implemented")
                    override val proj4: String
                        get() = TODO("Not yet implemented")
                    override val unit: String
                        get() = TODO("Not yet implemented")
                    override val semiMajor: Double
                        get() = TODO("Not yet implemented")
                    override val semiMinor: Double
                        get() = TODO("Not yet implemented")
                    override val invertAxis: Boolean
                        get() = TODO("Not yet implemented")
                    override val uri: URI
                        get() = TODO("Not yet implemented")
                    override val epsgCode: Int
                        get() = 4326
                    override val input: String
                        get() = crsString

                    override fun isSame(other: CRS): Boolean = other.epsgCode == epsgCode
                }
            }
        }

        override suspend fun afterSpec(spec: Spec) {
            unmockkObject(SpatialReferenceCRSFactory)
        }
    }

    override fun extensions(): List<Extension> = listOf(SpringExtension, JUnitExtensionAdapter(MockKExtension()), MockkGdalExtension())
}
