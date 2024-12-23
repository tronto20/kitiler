package dev.tronto.kitiler.core.outgoing.adaptor.gdal.path

import dev.tronto.kitiler.core.domain.Ordered
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import java.net.URI

class DefaultS3GdalPathProvider :
    GdalPathProvider,
    Ordered {
    companion object {
        private val ENABLED by lazy {
            System.getenv().run {
                !(get("KITILER_DISABLE_GDAL_PATH_S3_DEFAULT")?.equals("YES", ignoreCase = true) ?: false) ||
                    (containsKey("AWS_SECRET_ACCESS_KEY") && containsKey("AWS_ACCESS_KEY_ID")) ||
                    (containsKey("AWS_ROLE_ARN") && containsKey("AWS_WEB_IDENTITY_TOKEN_FILE"))
            }
        }
    }
    override fun getOrder(): Int = Int.MAX_VALUE - 1
    override suspend fun supports(uri: URI, openOptions: OptionProvider<OpenOption>): Boolean =
        ENABLED && uri.scheme == S3GdalPath.SCHEME

    override suspend fun toGdalPath(uri: URI, openOptions: OptionProvider<OpenOption>): GdalPath = S3GdalPath(uri)
}
