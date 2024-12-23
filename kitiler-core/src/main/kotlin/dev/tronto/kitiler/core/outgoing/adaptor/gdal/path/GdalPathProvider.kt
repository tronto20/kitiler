package dev.tronto.kitiler.core.outgoing.adaptor.gdal.path

import dev.tronto.kitiler.core.domain.Ordered
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import java.net.URI
import java.util.*

interface GdalPathProvider {
    suspend fun supports(uri: URI, openOptions: OptionProvider<OpenOption>): Boolean
    suspend fun toGdalPath(uri: URI, openOptions: OptionProvider<OpenOption>): GdalPath

    companion object {
        @JvmStatic
        val services by lazy {
            ServiceLoader.load(GdalPathProvider::class.java, Thread.currentThread().contextClassLoader)
                .toList().sortedBy { if (it is Ordered) it.order else 0 }
        }

        suspend fun of(uri: URI, openOptions: OptionProvider<OpenOption>): GdalPath? = services.find {
            it.supports(uri, openOptions)
        }?.toGdalPath(uri, openOptions)
    }
}
