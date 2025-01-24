package dev.tronto.kitiler.image.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.ImageFormat
import dev.tronto.kitiler.image.outgoing.port.ImageRenderer
import io.github.oshai.kotlinlogging.KotlinLogging

class JpegGdalRenderer : ImageRenderer {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger {}

        @JvmStatic
        private val SUPPORT_BAND = setOf(1, 3)
    }

    override fun supports(imageData: ImageData, format: ImageFormat): Boolean = format == ImageFormat.JPEG &&
        imageData.dataType == DataType.UInt8 &&
        imageData.bandCount in SUPPORT_BAND

    override suspend fun render(imageData: ImageData, format: ImageFormat): ByteArray =
        logger.logTrace("Render Gdal Jpeg") {
            val data = imageData.getBandBuffer()
            return GdalRenderer.render(
                "JPEG",
                imageData.width,
                imageData.height,
                imageData.bandCount,
                imageData.dataType,
                "image.jpeg"
            ) {
                // ignore mask
                write(data, IntArray(imageData.bandCount) { it + 1 })
            }
        }
}
