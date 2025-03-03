package dev.tronto.kitiler.spring.autoconfigure.webflux

import dev.tronto.kitiler.core.utils.withResourceContext
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.CoRouterFunctionDsl
import org.springframework.web.reactive.function.server.RequestPredicate
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Suppress("FunctionName")
fun CoRouterFunctionDsl.GET(patterns: List<String>, block: suspend (ServerRequest) -> ServerResponse) {
    GET(patterns.map(RequestPredicates::path).reduce(RequestPredicate::or)) { req ->
        withResourceContext { block(req) }
    }
}

fun ServerResponse.BodyBuilder.contentType(contentType: String) = contentType(MediaType.parseMediaType(contentType))
