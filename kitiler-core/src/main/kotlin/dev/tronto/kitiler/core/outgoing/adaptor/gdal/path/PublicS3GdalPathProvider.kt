package dev.tronto.kitiler.core.outgoing.adaptor.gdal.path

import dev.tronto.kitiler.core.domain.Ordered
import dev.tronto.kitiler.core.incoming.controller.option.EnvOption
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.incoming.controller.option.plus
import java.net.URI

class PublicS3GdalPathProvider :
    GdalPathProvider,
    Ordered {
    companion object {
        private val ENABLED by lazy {
            System.getenv().run {
                !(get("KITILER_DISABLE_GDAL_PATH_S3_PUBLIC")?.equals("YES", ignoreCase = true) ?: false)
            }
        }
    }

    override fun getOrder(): Int = Int.MAX_VALUE

    override suspend fun supports(uri: URI, openOptions: OptionProvider<OpenOption>): Boolean =
        ENABLED && uri.scheme == S3GdalPath.SCHEME

    override suspend fun toGdalPath(uri: URI, openOptions: OptionProvider<OpenOption>): GdalPath =
        S3GdalPath(uri, OptionProvider.empty<OpenOption>().plus(EnvOption("AWS_NO_SIGN_REQUEST", "YES")))
}
