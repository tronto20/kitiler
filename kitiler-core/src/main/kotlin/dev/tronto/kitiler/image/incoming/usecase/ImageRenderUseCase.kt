package dev.tronto.kitiler.image.incoming.usecase

import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.image.domain.Image
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.incoming.controller.option.RenderOption

interface ImageRenderUseCase {
    suspend fun renderImage(imageData: ImageData, renderOptions: OptionProvider<RenderOption>): Image
}
