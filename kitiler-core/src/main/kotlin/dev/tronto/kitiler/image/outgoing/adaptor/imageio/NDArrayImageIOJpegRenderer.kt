package dev.tronto.kitiler.image.outgoing.adaptor.imageio

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.domain.Ordered
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.ImageFormat
import dev.tronto.kitiler.image.outgoing.adaptor.multik.NDArrayImageData
import dev.tronto.kitiler.image.outgoing.port.ImageRenderer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.ndarray.operations.stack
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import org.jetbrains.kotlinx.multik.ndarray.operations.toIntArray
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 *  NDArray 를 사용하는 ImageData 를 Jpeg 포맷으로 렌더링.
 *
 *  ** native-image 에서 동작하지 않음. (jvm 21 기준) **
 */
class NDArrayImageIOJpegRenderer :
    ImageRenderer,
    Ordered {
    companion object {
        @JvmStatic
        private val SUPPORT_DATATYPE = listOf(DataType.UInt8)

        @JvmStatic
        private val SUPPORT_BAND = intArrayOf(3)

        @JvmStatic
        private val logger = KotlinLogging.logger { }
        init {
            ImageIORegistrar
        }
    }

    override fun getOrder(): Int = Int.MAX_VALUE

    override fun supports(imageData: ImageData, format: ImageFormat): Boolean = imageData is NDArrayImageData<*> &&
        format == ImageFormat.JPEG &&
        imageData.dataType in SUPPORT_DATATYPE &&
        imageData.band in SUPPORT_BAND

    override suspend fun render(imageData: ImageData, format: ImageFormat): ByteArray {
        require(supports(imageData, format))
        val data = (imageData as NDArrayImageData<Int>).data
        val mask = imageData.mask
        val shape = data.shape
        val width = shape[2]
        val height = shape[1]
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        logger.logTrace("write image jpeg") {
            val d3 = mk.stack(mask, mask, mask)
            val arr = data.times(d3).transpose(1, 2, 0).flatten().toIntArray()
            image.raster.setPixels(0, 0, width, height, arr)
        }
        return logger.logTrace("toByteArray()") {
            ByteArrayOutputStream().use {
                ImageIO.write(image, "JPEG", it)
                it.toByteArray()
            }
        }
    }
}
