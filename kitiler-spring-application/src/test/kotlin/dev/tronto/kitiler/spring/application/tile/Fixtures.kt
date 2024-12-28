package dev.tronto.kitiler.spring.application.tile

import dev.tronto.kitiler.spring.application.copy
import dev.tronto.kitiler.spring.application.core.infoFields
import dev.tronto.kitiler.spring.application.core.testInfo
import dev.tronto.kitiler.tile.domain.Point
import dev.tronto.kitiler.tile.domain.TileInfo
import dev.tronto.kitiler.tile.domain.TileMatrix
import dev.tronto.kitiler.tile.domain.TileMatrixSet
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation

val testTileInfo = TileInfo(10, 15, testInfo)
val tileInfoFields = infoFields.map {
    it.copy(path = "info.${it.path}")
} + listOf(
    PayloadDocumentation.fieldWithPath("minZoom").description("Minimum zoom of dataset.").type(JsonFieldType.NUMBER),
    PayloadDocumentation.fieldWithPath("maxZoom").description("Maximum zoom of dataset.").type(JsonFieldType.NUMBER)
)

val testTileMatrixSet = TileMatrixSet(
    "tileMatrixSet",
    "title",
    "uri",
    listOf("N", "E"),
    "EPSG:3857",
    null,
    listOf(
        TileMatrix(
            "10",
            null,
            null,
            null,
            0.0,
            0.0,
            null,
            Point(doubleArrayOf(0.0, 0.0)),
            256,
            256,
            10,
            10
        )
    )
)
