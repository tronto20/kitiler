package dev.tronto.kitiler.image.outgoing.adaptor.imageio

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.domain.Ordered
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.ImageFormat
import dev.tronto.kitiler.image.outgoing.adaptor.multik.NDArrayImageData
import dev.tronto.kitiler.image.outgoing.port.ImageRenderer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlinx.multik.ndarray.operations.toIntArray
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 *  NDArray 를 사용하는 ImageData 를 Png 포맷으로 렌더링.
 */
class NDArrayImageIOPNGRenderer :
    ImageRenderer,
    Ordered {
    companion object {
        @JvmStatic
        private val SUPPORT_DATATYPE = listOf(DataType.UInt8)

        @JvmStatic
        private val SUPPORT_BAND = intArrayOf(3, 4)

        @JvmStatic
        private val logger = KotlinLogging.logger { }

        @JvmStatic
        private val ALPHA_VALUE = 255
        init {
            ImageIORegistrar
        }
    }

    override fun getOrder(): Int = Int.MAX_VALUE

    override fun supports(imageData: ImageData, format: ImageFormat): Boolean = imageData is NDArrayImageData<*> &&
        format == ImageFormat.PNG &&
        imageData.data.shape.size == 3 &&
        imageData.dataType in SUPPORT_DATATYPE &&
        imageData.band in SUPPORT_BAND

    override suspend fun render(imageData: ImageData, format: ImageFormat): ByteArray {
        require(supports(imageData, format))
        val data = (imageData as NDArrayImageData<Int>).data
        val mask = imageData.mask

        val shape = data.shape
        val band = shape[0]
        val width = shape[2]
        val height = shape[1]

        val valueArray = data.toIntArray()
        val maskArray = mask.toIntArray()
        fun getValueIndex(b: Int, h: Int, w: Int) = (b * height * width) + (h * width) + w

        val targetArray = IntArray(width * height * 4)
        fun getTargetIndex(b: Int, h: Int, w: Int) = (h * width * 4) + (w * 4) + b

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        logger.logTrace("write image png") {
            when (band) {
                3 -> logger.logTrace("write image png 3band") {
                    for (h in 0..<height) {
                        index@ for (w in 0..<width) {
                            val maskValue = maskArray[getValueIndex(0, h, w)]
                            if (maskValue == 0) {
                                continue@index
                            }
                            targetArray[getTargetIndex(3, h, w)] = ALPHA_VALUE
                            for (b in 0..<3) {
                                targetArray[getTargetIndex(b, h, w)] = valueArray[getValueIndex(b, h, w)]
                            }
                        }
                    }
                }

                4 -> logger.logTrace("write image png 4band") {
                    for (h in 0..<height) {
                        index@ for (w in 0..<width) {
                            val maskValue = maskArray[getValueIndex(0, h, w)]
                            if (maskValue == 0) {
                                continue@index
                            }
                            val alphaValue = valueArray[getValueIndex(3, h, w)]
                            targetArray[getTargetIndex(3, h, w)] = alphaValue
                            for (b in 0..<3) {
                                targetArray[getTargetIndex(b, h, w)] = valueArray[getValueIndex(b, h, w)]
                            }
                        }
                    }
                }

                else -> throw UnsupportedOperationException("$band band not supported for png.")
            }
        }
        image.raster.setPixels(0, 0, width, height, targetArray)

        return logger.logTrace("toByteArray()") {
            ByteArrayOutputStream().use {
                ImageIO.write(image, "PNG", it)
                it.toByteArray()
            }
        }
    }
}
