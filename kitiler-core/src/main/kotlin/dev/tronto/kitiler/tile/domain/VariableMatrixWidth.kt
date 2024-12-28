package dev.tronto.kitiler.tile.domain

import kotlinx.serialization.Serializable

@Serializable
data class VariableMatrixWidth(val coalesce: Int, val minTileRow: Int, val maxTileRow: Int)
