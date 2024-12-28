package dev.tronto.kitiler.core.incoming.controller.option

import dev.tronto.kitiler.core.domain.ResamplingAlgorithm
import dev.tronto.kitiler.core.exception.RequiredParameterMissingException

class ResamplingOptionParser : OptionParser<ResamplingOption> {
    companion object {
        private const val PARAM = "resampling"
    }

    override val type: ArgumentType<ResamplingOption> = ArgumentType()

    override fun generateMissingException(): Exception = RequiredParameterMissingException(PARAM)

    override suspend fun parse(request: Request): ResamplingOption = request.parameter(PARAM).firstOrNull()
        ?.let { ResamplingOption(ResamplingAlgorithm(it)) }
        ?: ResamplingOption(ResamplingAlgorithm.NEAREST)

    override fun box(option: ResamplingOption): Map<String, List<String>> =
        mapOf(PARAM to listOf(option.algorithm.name.lowercase()))

    override fun descriptions(): List<OptionDescription<*>> = listOf(
        OptionDescription<String>(
            PARAM,
            "resampling algorithm.",
            ResamplingAlgorithm.NEAREST.name,
            enums = ResamplingAlgorithm.entries.map { it.name },
            default = ResamplingAlgorithm.NEAREST.name
        )
    )
}
