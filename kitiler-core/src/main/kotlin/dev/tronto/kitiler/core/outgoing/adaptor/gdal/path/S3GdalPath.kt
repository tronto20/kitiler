package dev.tronto.kitiler.core.outgoing.adaptor.gdal.path

import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import java.net.URI

class S3GdalPath(private val uri: URI, override val openOptions: OptionProvider<OpenOption> = OptionProvider.empty()) :
    GdalPath {
    companion object {
        const val SCHEME = "s3"
    }
    init {
        require(uri.scheme == SCHEME)
    }
    override fun toURI(): URI = uri
    override fun toPathString(): String = uri.toString().replace("$SCHEME://", "/vsis3/")
}
