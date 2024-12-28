package dev.tronto.kitiler.wmts.incoming.controller.option

import dev.tronto.kitiler.core.exception.IllegalParameterException
import dev.tronto.kitiler.core.exception.RequiredParameterMissingException
import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OptionDescription
import dev.tronto.kitiler.core.incoming.controller.option.OptionParser
import dev.tronto.kitiler.core.incoming.controller.option.Request

class MinZoomOptionParser : OptionParser<MinZoomOption> {
    companion object {
        private const val PARAM = "minzoom"
    }

    override val type: ArgumentType<MinZoomOption> = ArgumentType()

    override fun generateMissingException(): Exception = RequiredParameterMissingException(PARAM)

    override suspend fun parse(request: Request): MinZoomOption? = request.parameter(PARAM).firstOrNull()?.let {
        MinZoomOption(it.toIntOrNull() ?: throw IllegalParameterException("$PARAM must be an integer."))
    }

    override fun box(option: MinZoomOption): Map<String, List<String>> =
        mapOf(PARAM to listOf(option.minZoom.toString()))

    override fun descriptions(): List<OptionDescription<*>> =
        listOf(OptionDescription<Int>(PARAM, "Overwrite default minzoom.", 0))
}
