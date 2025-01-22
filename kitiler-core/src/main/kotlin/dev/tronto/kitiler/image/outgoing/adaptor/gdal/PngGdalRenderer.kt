package dev.tronto.kitiler.image.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.ImageFormat
import dev.tronto.kitiler.image.outgoing.port.ImageRenderer
import io.github.oshai.kotlinlogging.KotlinLogging

class PngGdalRenderer : ImageRenderer {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger {}

        @JvmStatic
        private val SUPPORT_BANDS = setOf(1, 3, 4)

        @JvmStatic
        private val SUPPORT_TYPE = setOf(DataType.UInt8, DataType.UInt16)
    }

    override fun supports(imageData: ImageData, format: ImageFormat): Boolean = format == ImageFormat.PNG &&
        imageData.dataType in SUPPORT_TYPE &&
        imageData.bandCount in SUPPORT_BANDS

    override suspend fun render(imageData: ImageData, format: ImageFormat): ByteArray =
        logger.logTrace("Render Gdal Png") {
            val data = imageData.getBandBuffer()
            val mask = imageData.getMaskBuffer()
            return GdalRenderer.render(
                "PNG",
                imageData.width,
                imageData.height,
                imageData.bandCount + 1,
                imageData.dataType,
                "image.png"
            ) {
                write(data, IntArray(imageData.bandCount) { it + 1 })
                write(mask, intArrayOf(imageData.bandCount))
            }
        }
}
