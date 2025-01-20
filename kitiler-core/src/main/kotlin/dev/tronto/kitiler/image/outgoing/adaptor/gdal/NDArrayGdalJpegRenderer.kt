package dev.tronto.kitiler.image.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.ImageFormat
import dev.tronto.kitiler.image.outgoing.adaptor.multik.NDArrayImageData
import dev.tronto.kitiler.image.outgoing.port.ImageRenderer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.operations.expandDims
import org.jetbrains.kotlinx.multik.ndarray.operations.stack
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import org.jetbrains.kotlinx.multik.ndarray.operations.toIntArray

class NDArrayGdalJpegRenderer : ImageRenderer {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger {}

        @JvmStatic
        private val SUPPORT_BAND = setOf(1, 3)
    }

    override fun supports(imageData: ImageData, format: ImageFormat): Boolean = format == ImageFormat.JPEG &&
        imageData.dataType == DataType.UInt8 &&
        imageData.band in SUPPORT_BAND

    override suspend fun render(imageData: ImageData, format: ImageFormat): ByteArray =
        logger.logTrace("Render Gdal Jpeg") {
            require(imageData is NDArrayImageData<*>)
            val data = imageData.data as D3Array<Int>
            val mask = imageData.mask
            val d3 = when (imageData.band) {
                1 -> mask.expandDims(0)
                3 -> mk.stack(mask, mask, mask)
                else -> throw IllegalStateException("Unsupported mask band: ${imageData.band}")
            }
            val dataArray = data.times(d3).toIntArray()
            return render(
                "JPEG",
                imageData.width,
                imageData.height,
                imageData.band,
                imageData.dataType,
                "image"
            ) {
                write(dataArray, IntArray(imageData.band) { it + 1 })
            }
        }
}
