package dev.tronto.kitiler.tile.domain

import dev.tronto.kitiler.tile.exception.TileCoordinateOutOfBoundsException
import kotlinx.serialization.Serializable

@Serializable
class TileMatrixSet(
    val id: String,
    val title: String? = null,
    val uri: String? = null,
    val orderedAxes: List<String>? = null,
    val crs: String? = null,
    // URL (http://, https://)
    val wellKnownScaleSet: String? = null,
    val tileMatrices: List<TileMatrix>,
    // TODO :: add boundingBox
) {
    override fun equals(other: Any?): Boolean = (other as? TileMatrixSet)?.id == id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "TileMatrixSet($id)"

    val minzoom: Int
        get() = sortedTileMatrices.firstOrNull()?.zoomLevel ?: 0
    val maxzoom: Int
        get() = sortedTileMatrices.lastOrNull()?.zoomLevel ?: 0
    val sortedTileMatrices: List<TileMatrix> by lazy {
        tileMatrices.sortedBy { it.zoomLevel }
    }

    private val tileMatrixZoomLevelMap: Map<Int, TileMatrix> by lazy {
        tileMatrices.associateBy { it.zoomLevel }
    }

    operator fun get(zoomLevel: Int) = tileMatrixZoomLevelMap[zoomLevel] ?: throw TileCoordinateOutOfBoundsException(
        "z",
        zoomLevel,
        tileMatrixZoomLevelMap.keys
    )
}
