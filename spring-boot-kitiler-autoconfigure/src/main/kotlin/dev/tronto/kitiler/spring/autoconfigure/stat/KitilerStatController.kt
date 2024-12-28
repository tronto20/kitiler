package dev.tronto.kitiler.spring.autoconfigure.stat

import dev.tronto.kitiler.core.incoming.controller.option.filter
import dev.tronto.kitiler.spring.autoconfigure.webflux.GET
import dev.tronto.kitiler.spring.autoconfigure.webflux.WebFluxOptionParserAdaptor
import dev.tronto.kitiler.stat.incoming.usecase.StatisticsUseCase
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Controller
class KitilerStatController(
    private val optionParser: WebFluxOptionParserAdaptor,
    private val pathProperties: KitilerStatPathProperties,
    private val statisticsUseCase: StatisticsUseCase,
) : RouterFunction<ServerResponse> by coRouter({
    GET(pathProperties.statistics) {
        val options = optionParser.parse(it)
        val statistics = statisticsUseCase.statistics(
            options.filter(),
            options.filter(),
            options.filter()
        )
        ok().bodyValueAndAwait(statistics)
    }
})
