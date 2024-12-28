package dev.tronto.kitiler.spring.autoconfigure.wmts

import dev.tronto.kitiler.core.incoming.controller.option.filter
import dev.tronto.kitiler.spring.autoconfigure.webflux.GET
import dev.tronto.kitiler.spring.autoconfigure.webflux.WebFluxOptionParserAdaptor
import dev.tronto.kitiler.wmts.incoming.usecase.WmtsUseCase
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Controller
class KitilerWmtsController(
    private val optionParser: WebFluxOptionParserAdaptor,
    private val wmtsPathProperties: KitilerWmtsPathProperties,
    private val wmtsUseCase: WmtsUseCase,
) : RouterFunction<ServerResponse> by coRouter({
    GET(wmtsPathProperties.capabilities) {
        val options = optionParser.parse(it)
        val document = wmtsUseCase.wmts(
            options.filter(),
            options.filter(),
            options.filter(),
            options.filter()
        )
        ok().bodyValueAndAwait(document)
    }
})
