package dev.tronto.kitiler.tile.service

import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.tile.domain.TileMatrixSet
import dev.tronto.kitiler.tile.incoming.controller.option.TileMatrixSetOption
import dev.tronto.kitiler.tile.incoming.usecase.TileMatrixSetUseCase
import dev.tronto.kitiler.tile.outgoing.port.TileMatrixSetFactory

class TileMatrixSetService(private val tileMatrixSetFactory: TileMatrixSetFactory) : TileMatrixSetUseCase {
    override suspend fun tileMatrixSets(): List<TileMatrixSet> = tileMatrixSetFactory.list().toList()

    override suspend fun tileMatrixSet(options: OptionProvider<TileMatrixSetOption>): TileMatrixSet =
        tileMatrixSetFactory.fromId(options.get(ArgumentType<TileMatrixSetOption>()).tileMatrixSetId)
}
