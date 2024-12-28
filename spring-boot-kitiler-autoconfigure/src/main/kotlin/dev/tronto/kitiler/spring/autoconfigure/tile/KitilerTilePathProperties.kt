package dev.tronto.kitiler.spring.autoconfigure.tile

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kitiler.web.path.tile")
data class KitilerTilePathProperties(
    val info: List<String> = listOf(
        "/tiles/{tileMatrixSetId}/info",
        "/tiles/info"
    ),
    val tiles: List<String> = listOf(
        "/tiles/{tileMatrixSetId}/{z}/{x}/{y}@{scale}x.{format}",
        "/tiles/{tileMatrixSetId}/{z}/{x}/{y}@{scale}x",
        "/tiles/{tileMatrixSetId}/{z}/{x}/{y}.{format}",
        "/tiles/{z}/{x}/{y}@{scale}x.{format}",
        "/tiles/{z}/{x}/{y}.{format}",
        "/tiles/{z}/{x}/{y}@{scale}x",
        "/tiles/{z}/{x}/{y}"
    ),
    val tileMatrixSets: List<String> = listOf(
        "/tileMatrixSets"
    ),
    val tileMatrixSet: List<String> = listOf(
        "/tileMatrixSets/{tileMatrixSetId}"
    ),
)
