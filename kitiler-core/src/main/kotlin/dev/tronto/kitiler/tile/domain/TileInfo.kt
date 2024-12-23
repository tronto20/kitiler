package dev.tronto.kitiler.tile.domain

import dev.tronto.kitiler.core.domain.Info
import kotlinx.serialization.Serializable

@Serializable
data class TileInfo(val minZoom: Int, val maxZoom: Int, val info: Info)
