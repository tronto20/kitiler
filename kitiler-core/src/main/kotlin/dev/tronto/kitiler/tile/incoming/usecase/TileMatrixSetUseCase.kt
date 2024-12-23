package dev.tronto.kitiler.tile.incoming.usecase

import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.tile.domain.TileMatrixSet
import dev.tronto.kitiler.tile.incoming.controller.option.TileMatrixSetOption

interface TileMatrixSetUseCase {
    suspend fun tileMatrixSets(): List<TileMatrixSet>
    suspend fun tileMatrixSet(options: OptionProvider<TileMatrixSetOption>): TileMatrixSet
}
