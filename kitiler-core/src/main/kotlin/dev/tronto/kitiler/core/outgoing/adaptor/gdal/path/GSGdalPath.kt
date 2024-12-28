package dev.tronto.kitiler.core.outgoing.adaptor.gdal.path

import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import java.net.URI

class GSGdalPath(private val uri: URI, override val openOptions: OptionProvider<OpenOption> = OptionProvider.empty()) :
    GdalPath {
    companion object {
        const val SCHEME = "gs"
    }
    init {
        require(uri.scheme == SCHEME)
    }
    override fun toURI(): URI = uri

    override fun toPathString(): String = uri.toString().replace("$SCHEME://", "/vsigs/")
}
