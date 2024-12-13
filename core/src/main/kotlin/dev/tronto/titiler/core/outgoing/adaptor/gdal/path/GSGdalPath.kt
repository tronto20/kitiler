package dev.tronto.titiler.core.outgoing.adaptor.gdal.path

import dev.tronto.titiler.core.incoming.controller.option.OpenOption
import dev.tronto.titiler.core.incoming.controller.option.OptionProvider
import java.net.URI

class GSGdalPath(
    private val uri: URI,
    override val openOptions: OptionProvider<OpenOption> = OptionProvider.empty(),
) : GdalPath {
    companion object {
        const val SCHEME = "gs"
    }
    init {
        require(uri.scheme == SCHEME)
    }
    override fun toURI(): URI {
        return uri
    }

    override fun toPathString(): String {
        return uri.toString().replace("$SCHEME://", "/vsigs/")
    }
}