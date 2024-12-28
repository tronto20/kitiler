package dev.tronto.kitiler.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class BandInfo(
    val bandIndex: BandIndex,
    val dataType: DataType,
    val description: String = "",
    val colorInterpolation: ColorInterpretation = ColorInterpretation.Undefined,
    val metadata: Map<String, String> = emptyMap(),
)
