package dev.tronto.titiler.spring.application.core.adaptor

import dev.tronto.titiler.core.incoming.controller.option.ArgumentType
import dev.tronto.titiler.core.incoming.controller.option.Option
import dev.tronto.titiler.core.incoming.controller.option.OptionParser
import dev.tronto.titiler.core.incoming.controller.option.OptionProvider
import dev.tronto.titiler.core.incoming.controller.option.OptionProviderImpl
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody
import java.util.*

class WebFluxOptionParserAdaptor(
    private val optionParserMap: Map<ArgumentType<out Option>, List<OptionParser<out Option>>>,
) {
    constructor() : this(
        ServiceLoader.load(OptionParser::class.java, Thread.currentThread().contextClassLoader).toList()
    )

    constructor(optionParsers: Iterable<OptionParser<out Option>>) : this(optionParsers.groupBy { it.type })

    suspend fun parse(request: ServerRequest, bodyKey: String? = null): OptionProvider<Option> {
        val bodyValue = bodyKey?.let { request.awaitBody<String>() }
        val adaptor = WebFluxRequestAdaptor(
            request,
            bodyKey,
            bodyValue
        )
        return OptionProviderImpl<Option>(
            adaptor,
            ArgumentType<Option>(),
            optionParserMap
        )
    }
}
