package dev.tronto.kitiler.image.incoming.usecase

import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.incoming.controller.option.ImageOption

interface ImageReadUseCase {
    suspend fun read(openOptions: OptionProvider<OpenOption>, imageOptions: OptionProvider<ImageOption>): ImageData
}
