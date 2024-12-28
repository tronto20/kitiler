package dev.tronto.kitiler.core.incoming.controller.option

import dev.tronto.kitiler.core.exception.IllegalParameterException
import dev.tronto.kitiler.core.exception.RequiredParameterMissingException

class NoDataOptionParser : OptionParser<NoDataOption> {
    companion object {
        private const val PARAM = "noData"
    }
    override val type: ArgumentType<NoDataOption> = ArgumentType()

    override fun generateMissingException(): Exception = RequiredParameterMissingException(PARAM)

    override suspend fun parse(request: Request): NoDataOption? = request.parameter(PARAM).firstOrNull()?.let {
        val double = it.toDoubleOrNull() ?: throw IllegalParameterException("noData must be a number: $it.")
        NoDataOption(double)
    }

    override fun box(option: NoDataOption): Map<String, List<String>> = mapOf(PARAM to listOf(option.noData.toString()))

    override fun descriptions(): List<OptionDescription<*>> = listOf(
        OptionDescription<Double>(PARAM, "Overwrite internal Nodata value.", 0.0)
    )
}
