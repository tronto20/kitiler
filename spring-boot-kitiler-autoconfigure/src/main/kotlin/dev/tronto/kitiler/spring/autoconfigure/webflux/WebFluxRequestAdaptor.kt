package dev.tronto.kitiler.spring.autoconfigure.webflux

import dev.tronto.kitiler.core.incoming.controller.option.Request
import org.springframework.web.reactive.function.server.ServerRequest

class WebFluxRequestAdaptor(
    val serverRequest: ServerRequest,
    val bodyKey: String? = null,
    val bodyValue: String? = null,
) : Request {
    private val queryParams = serverRequest.queryParams()
        .mapKeys { it.key.lowercase() }
        .mapValues { it.value.filter { it.isNotBlank() } }
    private val pathParams = serverRequest.pathVariables().mapKeys { it.key.lowercase() }

    private fun getFromQueryParameter(lowerKey: String): List<String>? {
        val queryParameters = queryParams[lowerKey]
        return queryParameters?.ifEmpty { null }
    }

    private fun getFromPathParameter(lowerKey: String): String? {
        return pathParams[lowerKey]?.let { return it }
    }

    private fun getFromBody(lowerKey: String): List<String>? = if (bodyKey?.lowercase() == lowerKey) {
        listOf()
    } else {
        null
    }

    override fun parameter(key: String): List<String> {
        val lowerKey = key.lowercase()
        getFromPathParameter(lowerKey)?.let { return listOf(it) }
        getFromQueryParameter(lowerKey)?.let { return it }
        getFromBody(lowerKey)?.let { return it }
        return emptyList()
    }

    private val headers = serverRequest.headers().asHttpHeaders()
        .mapKeys { it.key.lowercase() }
        .mapValues { it.value.filter { it.isNotBlank() } }

    override fun option(key: String): List<String> = headers[key.lowercase()] ?: emptyList()
}
