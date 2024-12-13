package dev.tronto.titiler.spring.application.swagger

import dev.tronto.titiler.spring.application.core.WebProperties
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.parser.OpenAPIV3Parser
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.resource.TransformedResource
import java.net.URI

@Controller
class SwaggerController(
    private val webProperties: WebProperties,
) : RouterFunction<ServerResponse> by coRouter({
    val openapi3Resource by lazy {
        val resolver = PathMatchingResourcePatternResolver()
        val openapi3Resource = resolver.getResources("classpath*:/swagger/openapi3.json").first()

        val parser = OpenAPIV3Parser()
        val openapi3 = parser.readContents(openapi3Resource.getContentAsString(Charsets.UTF_8)).openAPI
        openapi3.servers = listOf(
            Server().apply {
                url = webProperties.baseUri
            }
        )

        TransformedResource(openapi3Resource, Json.mapper().writeValueAsBytes(openapi3))
    }

    GET("/") {
        temporaryRedirect(URI.create("swagger")).buildAndAwait()
    }
    GET("swagger") {
        ok().bodyValueAndAwait(ClassPathResource("swagger/swagger.html"))
    }
    GET("swagger/api.json") {
        ok().bodyValueAndAwait(openapi3Resource)
    }
})
