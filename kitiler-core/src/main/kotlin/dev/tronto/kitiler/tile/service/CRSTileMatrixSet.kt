package dev.tronto.kitiler.tile.service

import dev.tronto.kitiler.core.outgoing.port.CRS
import dev.tronto.kitiler.tile.domain.TileMatrix
import dev.tronto.kitiler.tile.domain.TileMatrixSet
import dev.tronto.kitiler.tile.domain.ZoomLevelStrategy
import dev.tronto.kitiler.tile.exception.UnsupportedUnitException
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal class CRSTileMatrixSet(val tileMatrixSet: TileMatrixSet, val crs: CRS) {
    val minzoom get() = tileMatrixSet.minzoom
    val maxzoom get() = tileMatrixSet.maxzoom
    private val unitFactors: Map<String, Double> = mapOf(
        "metre" to 1.0,
        "degree" to 2.0 * PI * crs.semiMajor / 360.0,
        "foot" to 0.3048,
        "US survey foot" to 0.30480060960121924
    )
    private val metersPerUnit: Double = unitFactors[crs.unit] ?: throw UnsupportedUnitException(
        crs.name,
        crs.unit,
        null
    )

    val isInvertAxis: Boolean = tileMatrixSet.orderedAxes
        ?.let { it[0].uppercase() in listOf("Y", "LAT", "N") }
        ?: crs.invertAxis

    fun zoomForResolution(
        resolution: Double,
        maxZoom: Int = maxzoom,
        minZoom: Int = minzoom,
        zoomLevelStrategy: ZoomLevelStrategy = ZoomLevelStrategy.AUTO,
    ): Int {
        val zoomRange = (minZoom..maxZoom)
        val matrix =
            tileMatrixSet.sortedTileMatrices.asSequence().filter {
                zoomRange.contains(it.zoomLevel)
            }.find {
                /**
                 *
                 *  From note g in http://docs.opengeospatial.org/is/17-083r2/17-083r2.html#table_2:
                 *  The pixel size of the tile can be obtained from the scaleDenominator
                 *  by multiplying the later by 0.28 10-3 / metersPerUnit.
                 */
                val matrixResolution = this.resolution(it)
                resolution > matrixResolution || abs(resolution - matrixResolution) / matrixResolution <= 1e-8
            } ?: tileMatrixSet.sortedTileMatrices.last()

        return if (matrix.zoomLevel > 0 && abs(resolution - resolution(matrix)) / resolution(matrix) > 1e-8) {
            when (zoomLevelStrategy) {
                ZoomLevelStrategy.LOWER -> {
                    max(matrix.zoomLevel - 1, minZoom)
                }

                ZoomLevelStrategy.UPPER -> {
                    min(matrix.zoomLevel, maxZoom)
                }

                ZoomLevelStrategy.AUTO -> {
                    val option = tileMatrixSet[max(matrix.zoomLevel - 1, minZoom)]
                    if (resolution(option) / resolution < resolution / resolution(matrix)) {
                        max(matrix.zoomLevel - 1, minZoom)
                    } else {
                        matrix.zoomLevel
                    }
                }
            }
        } else {
            matrix.zoomLevel
        }
    }

    private val resolutionCache = mutableMapOf<String, Double>()
    fun resolution(matrix: TileMatrix): Double = resolutionCache.getOrPut(matrix.id) {
        matrix.scaleDenominator * 0.28e-3 / metersPerUnit
    }
}
