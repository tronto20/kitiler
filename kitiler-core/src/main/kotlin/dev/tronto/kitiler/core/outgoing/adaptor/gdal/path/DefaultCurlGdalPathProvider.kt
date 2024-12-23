package dev.tronto.kitiler.core.outgoing.adaptor.gdal.path

import dev.tronto.kitiler.core.domain.Ordered
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import java.net.URI

class DefaultCurlGdalPathProvider :
    GdalPathProvider,
    Ordered {
    companion object {
        private val ENABLED by lazy {
            System.getenv().run {
                !(get("KITILER_DISABLE_GDAL_PATH_CURL_DEFAULT")?.equals("YES", ignoreCase = true) ?: false)
            }
        }
    }
    override fun getOrder(): Int = Int.MAX_VALUE

    override suspend fun supports(uri: URI, openOptions: OptionProvider<OpenOption>): Boolean =
        ENABLED && uri.scheme in CurlGdalPath.SCHEME_LIST

    override suspend fun toGdalPath(uri: URI, openOptions: OptionProvider<OpenOption>): GdalPath = CurlGdalPath(uri)
}
