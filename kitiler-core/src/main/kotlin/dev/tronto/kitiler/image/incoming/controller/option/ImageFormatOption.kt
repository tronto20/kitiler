package dev.tronto.kitiler.image.incoming.controller.option

import dev.tronto.kitiler.image.domain.ImageFormat

@JvmInline
value class ImageFormatOption(val format: ImageFormat) : RenderOption
