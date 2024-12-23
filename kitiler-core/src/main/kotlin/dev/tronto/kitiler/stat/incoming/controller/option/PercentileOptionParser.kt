package dev.tronto.kitiler.stat.incoming.controller.option

import dev.tronto.kitiler.core.exception.IllegalParameterException
import dev.tronto.kitiler.core.exception.RequiredParameterMissingException
import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OptionDescription
import dev.tronto.kitiler.core.incoming.controller.option.OptionParser
import dev.tronto.kitiler.core.incoming.controller.option.Request
import dev.tronto.kitiler.stat.domain.Percentile

class PercentileOptionParser : OptionParser<PercentileOption> {
    companion object {
        private const val PARAM = "percentile"
    }

    override val type: ArgumentType<PercentileOption> = ArgumentType()

    override fun generateMissingException(): Exception = RequiredParameterMissingException(PARAM)

    override suspend fun parse(request: Request): PercentileOption? {
        val percentiles = request.parameter(PARAM).flatMap {
            it.split(',')
        }.map {
            it.toIntOrNull() ?: throw IllegalParameterException("$PARAM must be integer.")
        }
        return if (percentiles.isEmpty()) {
            null
        } else {
            PercentileOption(
                percentiles.map {
                    try {
                        Percentile(it)
                    } catch (e: IllegalArgumentException) {
                        throw IllegalParameterException(e.message, e)
                    }
                }
            )
        }
    }

    override fun box(option: PercentileOption): Map<String, List<String>> = mapOf(
        PARAM to option.percentiles.map {
            it.toString()
        }
    )

    override fun descriptions(): List<OptionDescription<*>> =
        listOf(OptionDescription(PARAM, "List of percentile values", arrayOf(2, 98), default = arrayOf(2, 98)))
}
