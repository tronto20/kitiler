package dev.tronto.titiler.core.outgoing.adaptor.gdal.path

import dev.tronto.titiler.core.domain.Ordered
import dev.tronto.titiler.core.incoming.controller.option.OpenOption
import dev.tronto.titiler.core.incoming.controller.option.OptionProvider
import java.net.URI

class DefaultCurlGdalPathProvider : GdalPathProvider, Ordered {
    companion object {
        private val ENABLED by lazy {
            System.getenv().run {
                !(get("TITILER_DISABLE_GDAL_PATH_CURL_DEFAULT")?.equals("YES", ignoreCase = true) ?: false)
            }
        }
    }
    override fun getOrder(): Int {
        return Int.MAX_VALUE
    }

    override suspend fun supports(uri: URI, openOptions: OptionProvider<OpenOption>): Boolean {
        return ENABLED && uri.scheme in CurlGdalPath.SCHEME_LIST
    }

    override suspend fun toGdalPath(uri: URI, openOptions: OptionProvider<OpenOption>): GdalPath {
        return CurlGdalPath(uri)
    }
}
