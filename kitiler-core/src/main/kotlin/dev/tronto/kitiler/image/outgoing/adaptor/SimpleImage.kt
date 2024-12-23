package dev.tronto.kitiler.image.outgoing.adaptor

import dev.tronto.kitiler.core.domain.OptionContext
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.image.domain.Image
import dev.tronto.kitiler.image.domain.ImageFormat

class SimpleImage(
    override val data: ByteArray,
    override val format: ImageFormat,
    vararg val options: OptionProvider<*>,
) : Image,
    OptionContext by OptionContext.wrap(*options)
