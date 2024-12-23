package dev.tronto.kitiler.image.service

import dev.tronto.kitiler.core.domain.OptionContext
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.incoming.controller.option.getOrNull
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.Image
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.ImageFormat
import dev.tronto.kitiler.image.incoming.controller.option.ImageFormatOption
import dev.tronto.kitiler.image.incoming.controller.option.RenderOption
import dev.tronto.kitiler.image.incoming.controller.option.RescaleOption
import dev.tronto.kitiler.image.incoming.usecase.ImageRenderUseCase
import dev.tronto.kitiler.image.outgoing.adaptor.SimpleImage
import dev.tronto.kitiler.image.outgoing.port.ImageDataAutoRescale
import dev.tronto.kitiler.image.outgoing.port.ImageRenderer
import io.github.oshai.kotlinlogging.KotlinLogging

class ImageRenderService(
    private val imageRenderers: List<ImageRenderer> = ImageRenderer.services,
    private val imageDataAutoRescales: List<ImageDataAutoRescale> = ImageDataAutoRescale.services,
) : ImageRenderUseCase {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }
    }

    private suspend fun tryRender(imageData: ImageData, format: ImageFormat): Image? {
        imageRenderers.forEach {
            if (it.supports(imageData, format)) {
                val imageBytes = it.render(imageData, format)
                return SimpleImage(imageBytes, format)
            }
        }
        return null
    }

    private fun wrapOptions(imageData: ImageData, image: Image, vararg options: OptionProvider<*>): Image {
        if (image is OptionContext) {
            val optionProviders = if (imageData is OptionContext) {
                imageData.getAllOptionProviders()
            } else {
                emptyList()
            }
            image.put(*options, *optionProviders.toTypedArray())
        }
        return image
    }

    override suspend fun renderImage(imageData: ImageData, renderOptions: OptionProvider<RenderOption>): Image =
        logger.logTrace("image render") {
            val rescaleOption: RescaleOption? = renderOptions.getOrNull()
            val formatOption: ImageFormatOption? = renderOptions.getOrNull()

            val rescaledImageData = if (rescaleOption != null) {
                imageData.rescaleToUInt8(rescaleOption.rescale)
            } else {
                imageData
            }

            val format = if (formatOption == null || formatOption.format == ImageFormat.AUTO) {
                if (rescaledImageData.masked) {
                    ImageFormat.PNG
                } else {
                    ImageFormat.JPEG
                }
            } else {
                formatOption.format
            }

            tryRender(rescaledImageData, format)?.let { return@logTrace wrapOptions(imageData, it, renderOptions) }

            imageDataAutoRescales.forEach {
                if (it.supports(rescaledImageData, format)) {
                    val autoRescaled = it.rescale(rescaledImageData, format)
                    tryRender(autoRescaled, format)?.let {
                        logger.warn {
                            "Invalid type: ${rescaledImageData.dataType} for the $format driver. " +
                                "It will be auto rescaled."
                        }
                        return@logTrace wrapOptions(imageData, it, renderOptions)
                    }
                }
            }

            throw UnsupportedOperationException("Image cannot rendered.")
        }
}
