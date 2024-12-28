package dev.tronto.kitiler.spring.autoconfigure.wmts

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kitiler.web.path.wmts")
data class KitilerWmtsPathProperties(
    val capabilities: List<String> = listOf(
        "/{tileMatrixSetId}/WMTSCapabilities.xml",
        "/WMTSCapabilities.xml"
    ),
)
