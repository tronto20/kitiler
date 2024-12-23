package dev.tronto.kitiler.wmts.service

import org.thymeleaf.context.Context
import org.thymeleaf.context.IContext
import java.util.*

class WmtsContext private constructor(
    val title: String,
    val requestUri: String,
    val layers: List<Layer>,
    val bboxCrsType: String,
    val bboxCrsUri: String,
    val mediaType: String,
    val tileMatrixSet: TileMatrixSet,
    private val context: Context,
) : IContext by context {
    companion object {
        @JvmStatic
        private val LOCALE = Locale.ENGLISH
    }

    constructor(
        title: String,
        requestUri: String,
        layers: List<Layer>,
        bboxCrsType: String,
        bboxCrsUri: String,
        mediaType: String,
        tileMatrixSet: TileMatrixSet,
    ) : this(
        title,
        requestUri,
        layers,
        bboxCrsType,
        bboxCrsUri,
        mediaType,
        tileMatrixSet,
        Context(
            LOCALE,
            mapOf(
                "title" to title,
                "requestUri" to requestUri,
                "layers" to layers,
                "bboxCrsType" to bboxCrsType,
                "bboxCrsUri" to bboxCrsUri,
                "mediaType" to mediaType,
                "tileMatrixSet" to tileMatrixSet
            )
        )
    )

    data class Layer(val title: String, val name: String, val bounds: DoubleArray, val tilesUrl: String)

    data class TileMatrixSet(val id: String, val crs: String, val tileMatrices: List<TileMatrix>)

    data class TileMatrix(
        val id: String,
        val scaleDenominator: Double,
        val topLeftCorner: DoubleArray,
        val tileWidth: Int,
        val tileHeight: Int,
        val matrixWidth: Int,
        val matrixHeight: Int,
    )
}
