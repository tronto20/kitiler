package dev.tronto.kitiler.spring.autoconfigure.core

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kitiler.web.path.core")
data class KitilerCorePathProperties(
    val bounds: List<String> = listOf("/bounds"),
    val info: List<String> = listOf("/info"),
)
