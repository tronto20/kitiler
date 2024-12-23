package dev.tronto.kitiler.tile.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class TileMatrix(
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val keywords: List<String>? = null,
    val scaleDenominator: Double,
    val cellSize: Double,
    val cornerOfOrigin: String? = null,
    val pointOfOrigin: Point,
    val matrixWidth: Int,
    val matrixHeight: Int,
    val tileWidth: Int,
    val tileHeight: Int,
    val variableMatrixWidths: List<VariableMatrixWidth>? = null,
) {
    @Transient
    val zoomLevel: Int = id.toIntOrNull() ?: 0
}
