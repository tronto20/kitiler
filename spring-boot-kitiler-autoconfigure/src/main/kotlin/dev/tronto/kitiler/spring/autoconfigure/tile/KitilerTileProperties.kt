package dev.tronto.kitiler.spring.autoconfigure.tile

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kitiler.tile")
data class KitilerTileProperties(val tileMatrixSetResourcePattern: String = "classpath*:tile/matrixset/*.json")
