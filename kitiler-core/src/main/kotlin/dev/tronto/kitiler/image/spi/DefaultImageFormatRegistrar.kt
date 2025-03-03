package dev.tronto.kitiler.image.spi

import dev.tronto.kitiler.image.domain.ImageFormat

class DefaultImageFormatRegistrar : ImageFormatRegistrar {
    override fun imageFormats(): Iterable<ImageFormat> = listOf(
        ImageFormat.AUTO,
        ImageFormat.JPEG,
        ImageFormat.PNG,
        ImageFormat.NPY,
        ImageFormat.NPZ,
        ImageFormat.WEBP,
        ImageFormat.JP2
    )
}
