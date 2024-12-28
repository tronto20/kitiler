package dev.tronto.kitiler.core.incoming.controller.option

import dev.tronto.kitiler.core.exception.RequiredParameterMissingException

class CRSOptionParser : OptionParser<CRSOption> {
    companion object {
        private const val PARAM = "dstCrs"
    }

    override val type: ArgumentType<CRSOption> = ArgumentType()

    override fun generateMissingException(): Exception = RequiredParameterMissingException(PARAM)

    override suspend fun parse(request: Request): CRSOption? = request.parameter(PARAM).firstOrNull()?.let {
        CRSOption(it)
    }

    override fun box(option: CRSOption): Map<String, List<String>> = mapOf(PARAM to listOf(option.crsString))

    override fun descriptions(): List<OptionDescription<*>> = listOf(
        OptionDescription<String>(PARAM, "target crs.", "EPSG:4326")
    )
}
