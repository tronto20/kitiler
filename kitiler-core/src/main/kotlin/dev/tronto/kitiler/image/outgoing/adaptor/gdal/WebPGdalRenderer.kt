package dev.tronto.kitiler.image.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.ImageFormat
import dev.tronto.kitiler.image.domain.IntArrayDataBuffer
import dev.tronto.kitiler.image.outgoing.port.ImageRenderer
import io.github.oshai.kotlinlogging.KotlinLogging

class WebPGdalRenderer : ImageRenderer {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger {}

        @JvmStatic
        private val SUPPORT_BAND = setOf(1, 3)
    }

    override fun supports(imageData: ImageData, format: ImageFormat): Boolean = format == ImageFormat.WEBP &&
        imageData.dataType == DataType.UInt8 &&
        imageData.bandCount in SUPPORT_BAND

    override suspend fun render(imageData: ImageData, format: ImageFormat): ByteArray =
        logger.logTrace("Render Gdal Jpeg") {
            return GdalRenderer.render(
                "WEBP",
                imageData.width,
                imageData.height,
                3,
                imageData.dataType,
                "image.webp",
                if (imageData.bandCount == 1) {
                    val bandBuffer = imageData.getBandBuffer()
                    val data = bandBuffer.intArray
                    val result = IntArray(data.size)
                    data.copyInto(result, 0)
                    data.copyInto(result, data.size)
                    data.copyInto(result, data.size + data.size)
                    IntArrayDataBuffer(result)
                } else {
                    imageData.getBandBuffer()
                },
                imageData.getValidArray()?.let { valid ->
                    IntArrayDataBuffer(
                        IntArray(imageData.width * imageData.height) {
                            if (valid[it]) 255 else 0
                        }
                    )
                }
            )
        }
}
