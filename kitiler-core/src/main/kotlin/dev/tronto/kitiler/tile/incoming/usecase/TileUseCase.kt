package dev.tronto.kitiler.tile.incoming.usecase

import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.tile.incoming.controller.option.TileOption

interface TileUseCase {
    suspend fun tile(openOptions: OptionProvider<OpenOption>, tileOptions: OptionProvider<TileOption>): ImageData
}
