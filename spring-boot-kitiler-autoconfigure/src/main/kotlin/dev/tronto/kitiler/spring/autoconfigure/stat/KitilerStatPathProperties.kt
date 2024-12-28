package dev.tronto.kitiler.spring.autoconfigure.stat

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("kitiler.web.path.stat")
data class KitilerStatPathProperties(val statistics: List<String> = listOf("/statistics"))
