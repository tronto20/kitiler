package dev.tronto.kitiler.core.outgoing.adaptor.gdal.path

import dev.tronto.kitiler.core.domain.Ordered
import dev.tronto.kitiler.core.incoming.controller.option.EnvOption
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.incoming.controller.option.plus
import java.net.URI

class PublicGSGdalPathProvider :
    GdalPathProvider,
    Ordered {
    companion object {
        private val ENABLED by lazy {
            System.getenv().run {
                !(get("KITILER_DISABLE_GDAL_PATH_GS_PUBLIC")?.equals("YES", ignoreCase = true) ?: false)
            }
        }
    }

    override fun getOrder(): Int = Int.MAX_VALUE

    override suspend fun supports(uri: URI, openOptions: OptionProvider<OpenOption>): Boolean =
        ENABLED && uri.scheme == GSGdalPath.SCHEME

    override suspend fun toGdalPath(uri: URI, openOptions: OptionProvider<OpenOption>): GdalPath =
        GSGdalPath(uri, OptionProvider.empty<OpenOption>().plus(EnvOption("GS_NO_SIGN_REQUEST", "YES")))
}
