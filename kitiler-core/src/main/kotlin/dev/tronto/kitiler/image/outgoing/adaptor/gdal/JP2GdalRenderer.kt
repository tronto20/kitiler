package dev.tronto.kitiler.image.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.ImageFormat
import dev.tronto.kitiler.image.outgoing.port.ImageRenderer
import io.github.oshai.kotlinlogging.KotlinLogging

class JP2GdalRenderer : ImageRenderer {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }

        private val SUPPORT_DATATYPE = setOf(DataType.UInt8, DataType.UInt16, DataType.Int16)
    }

    override fun supports(imageData: ImageData, format: ImageFormat): Boolean =
        format == ImageFormat.JP2 && imageData.dataType in SUPPORT_DATATYPE

    override suspend fun render(imageData: ImageData, format: ImageFormat): ByteArray =
        logger.logTrace("Render Gdal JP2") {
            return GdalRenderer.render(
                "JP2",
                imageData.width,
                imageData.height,
                imageData.bandCount,
                imageData.dataType,
                "image.jp2",
                imageData.getBandBuffer(),
                imageData.getValidArray()?.toDataBuffer()
            )
        }
}
