package dev.tronto.kitiler.image.incoming.usecase

import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.incoming.controller.option.get
import dev.tronto.kitiler.core.incoming.controller.option.getOrNull
import dev.tronto.kitiler.core.incoming.controller.option.plus
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.incoming.controller.option.FeatureOption
import dev.tronto.kitiler.image.incoming.controller.option.ImageOption
import dev.tronto.kitiler.image.incoming.controller.option.ImageSizeOption
import dev.tronto.kitiler.image.incoming.controller.option.MaxSizeOption
import dev.tronto.kitiler.image.incoming.controller.option.WindowOption

interface ImageBBoxUseCase : ImageReadUseCase {
    suspend fun bbox(openOptions: OptionProvider<OpenOption>, imageOptions: OptionProvider<ImageOption>): ImageData {
        val windowOption: WindowOption = imageOptions.get()
        val imageSizeOption: ImageSizeOption? = imageOptions.getOrNull()
        val maxSizeOption: MaxSizeOption? = imageOptions.getOrNull()
        val overrideImageOptions = if (imageSizeOption == null && maxSizeOption == null) {
            imageOptions.filterNot(ArgumentType<FeatureOption>()) + MaxSizeOption(1024)
        } else {
            imageOptions.filterNot(ArgumentType<FeatureOption>())
        }
        return read(
            openOptions,
            overrideImageOptions
        )
    }
}
