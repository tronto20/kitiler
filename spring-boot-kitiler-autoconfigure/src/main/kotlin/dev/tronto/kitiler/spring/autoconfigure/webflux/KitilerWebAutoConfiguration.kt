package dev.tronto.kitiler.spring.autoconfigure.webflux

import dev.tronto.kitiler.core.incoming.controller.option.OptionParser
import dev.tronto.kitiler.spring.autoconfigure.utils.sortedByOrdered
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.support.GenericApplicationContext
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import java.net.URI
import java.util.function.Supplier

@AutoConfiguration
@ConditionalOnClass(CorsWebFilter::class)
@EnableConfigurationProperties(KitilerWebProperties::class, KitilerCorsProperties::class)
class KitilerWebAutoConfiguration(applicationContext: GenericApplicationContext) {
    init {
        OptionParser.services.forEach {
            applicationContext.defaultListableBeanFactory.registerBeanDefinition(
                it::class.qualifiedName ?: it.toString(),
                GenericBeanDefinition().apply {
                    setBeanClass(it::class.java)
                    instanceSupplier = Supplier { it }
                }
            )
        }
    }

    @Bean
    fun webFluxOptionParserAdaptor(optionParsers: ObjectProvider<OptionParser<*>>): WebFluxOptionParserAdaptor =
        WebFluxOptionParserAdaptor(optionParsers.sortedByOrdered())

    @Bean
    fun corsWebFilter(
        kitilerWebProperties: KitilerWebProperties,
        kitilerCorsProperties: KitilerCorsProperties,
    ): CorsWebFilter {
        val config = CorsConfiguration().apply {
            allowedOrigins = kitilerCorsProperties.allowedOrigins
            allowedOriginPatterns = kitilerCorsProperties.allowedOriginPatterns
            maxAge = kitilerCorsProperties.maxAge
            allowedMethods = kitilerCorsProperties.allowedMethods
            allowedHeaders = kitilerCorsProperties.allowedHeaders
            exposedHeaders = kitilerCorsProperties.exposedHeaders
            allowCredentials = kitilerCorsProperties.allowCredentials
        }
        val source = UrlBasedCorsConfigurationSource().apply {
            val basePath = URI.create(kitilerWebProperties.baseUri).path
            registerCorsConfiguration("$basePath/**", config)
        }

        return CorsWebFilter(source)
    }
}
