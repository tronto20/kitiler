package dev.tronto.kitiler.tile.outgoing.adaptor.resource

import dev.tronto.kitiler.tile.domain.TileMatrixSet
import dev.tronto.kitiler.tile.exception.UnsupportedTileMatrixSetException
import dev.tronto.kitiler.tile.outgoing.port.TileMatrixSetFactory
import kotlinx.serialization.json.Json
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

class ResourceTileMatrixSetFactory(resourcePattern: String = "classpath*:tile/matrixset/*.json") :
    TileMatrixSetFactory {
    private fun loads(pattern: String): List<TileMatrixSet> {
        val resolver = PathMatchingResourcePatternResolver()
        return resolver.getResources(pattern).mapNotNull {
            runCatching {
                Json.decodeFromString<TileMatrixSet>(it.getContentAsString(Charsets.UTF_8))
            }.getOrNull()
        }.toList()
    }

    private val tileMatrixSetMap = loads(resourcePattern).associateBy { it.id.lowercase() }

    override suspend fun list(): Iterable<TileMatrixSet> = tileMatrixSetMap.values

    override suspend fun fromId(id: String): TileMatrixSet =
        tileMatrixSetMap[id.lowercase()] ?: throw UnsupportedTileMatrixSetException(id)
}
