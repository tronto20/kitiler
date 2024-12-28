package dev.tronto.kitiler.spring.autoconfigure.webflux

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kitiler.web.cors")
data class KitilerCorsProperties(
    val allowedOrigins: List<String> = listOf(),
    val allowedOriginPatterns: List<String> = listOf(),
    val maxAge: Long = 3600,
    val allowedMethods: List<String> = listOf("GET", "POST"),
    val allowedHeaders: List<String> = listOf(),
    val exposedHeaders: List<String> = listOf(),
    val allowCredentials: Boolean = false,
)
