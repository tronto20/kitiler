package dev.tronto.kitiler.tile.incoming.usecase

import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.tile.domain.TileInfo
import dev.tronto.kitiler.tile.incoming.controller.option.TileOption

interface TileInfoUseCase {
    suspend fun tileInfo(openOptions: OptionProvider<OpenOption>, tileOptions: OptionProvider<TileOption>): TileInfo
}
