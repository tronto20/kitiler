package dev.tronto.kitiler.spring.autoconfigure.webflux

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kitiler.web")
data class KitilerWebProperties(val baseUri: String = "http://localhost:8080")
