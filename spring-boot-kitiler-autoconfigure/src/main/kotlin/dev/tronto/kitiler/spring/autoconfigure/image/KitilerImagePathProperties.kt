package dev.tronto.kitiler.spring.autoconfigure.image

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kitiler.web.path.image")
data class KitilerImagePathProperties(
    val bbox: List<String> = listOf(
        "/bbox/{minx},{miny},{maxx},{maxy}/{width}x{height}.{format}",
        "/bbox/{minx},{miny},{maxx},{maxy}/{width}x{height}",
        "/bbox/{minx},{miny},{maxx},{maxy}.{format}",
        "/bbox/{minx},{miny},{maxx},{maxy}"
    ),
    val preview: List<String> = listOf(
        "/preview.{format}",
        "/preview"
    ),
)
