package dev.tronto.kitiler.stat.domain

import dev.tronto.kitiler.core.domain.BandIndex
import kotlinx.serialization.Serializable

@Serializable
data class BandStatistics(
    val bandIndex: BandIndex,
    val min: Double,
    val max: Double,
    val mean: Double,
    val count: Int,
    val sum: Double,
    val std: Double,
    val median: Double,
    val majority: Double,
    val minority: Double,
    val unique: Int,
    val validPercent: Double,
    val maskedPixels: Int,
    val validPixels: Int,
    val percentiles: List<PercentileResult>,
) {
    @Serializable
    data class PercentileResult(val percentile: Percentile, val value: Double)
}
