package dev.tronto.kitiler.tile.outgoing.port

import dev.tronto.kitiler.tile.domain.TileMatrixSet

interface TileMatrixSetFactory {
    suspend fun default(): TileMatrixSet = fromId("WebMercatorQuad")
    suspend fun list(): Iterable<TileMatrixSet>
    suspend fun fromId(id: String): TileMatrixSet
}
