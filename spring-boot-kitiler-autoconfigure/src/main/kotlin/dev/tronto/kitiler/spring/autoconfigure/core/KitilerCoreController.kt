package dev.tronto.kitiler.spring.autoconfigure.core

import dev.tronto.kitiler.core.incoming.controller.option.filter
import dev.tronto.kitiler.core.incoming.usecase.BoundsUseCase
import dev.tronto.kitiler.core.incoming.usecase.InfoUseCase
import dev.tronto.kitiler.spring.autoconfigure.webflux.GET
import dev.tronto.kitiler.spring.autoconfigure.webflux.WebFluxOptionParserAdaptor
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Controller
class KitilerCoreController(
    private val optionParser: WebFluxOptionParserAdaptor,
    private val kitilerCorePathProperties: KitilerCorePathProperties,
    private val boundsUseCase: BoundsUseCase,
    private val infoUseCase: InfoUseCase,
) : RouterFunction<ServerResponse> by coRouter({
    GET(kitilerCorePathProperties.bounds) {
        val options = optionParser.parse(it)
        val bounds = boundsUseCase.getBounds(options.filter())
        ok().bodyValueAndAwait(bounds)
    }

    GET(kitilerCorePathProperties.info) {
        val options = optionParser.parse(it)
        val info = infoUseCase.getInfo(options.filter())
        ok().bodyValueAndAwait(info)
    }
})
