package dev.tronto.kitiler.image.incoming.usecase

import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.incoming.controller.option.getOrNull
import dev.tronto.kitiler.core.incoming.controller.option.plus
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.incoming.controller.option.ImageOption
import dev.tronto.kitiler.image.incoming.controller.option.ImageSizeOption
import dev.tronto.kitiler.image.incoming.controller.option.MaxSizeOption

interface ImagePreviewUseCase : ImageReadUseCase {
    suspend fun preview(openOptions: OptionProvider<OpenOption>, imageOptions: OptionProvider<ImageOption>): ImageData {
        val imageSizeOption: ImageSizeOption? = imageOptions.getOrNull()
        val maxSizeOption: MaxSizeOption? = imageOptions.getOrNull()
        val overrideImageOptions = if (imageSizeOption == null && maxSizeOption == null) {
            imageOptions + MaxSizeOption(1024)
        } else {
            imageOptions
        }
        return read(openOptions, overrideImageOptions)
    }
}
