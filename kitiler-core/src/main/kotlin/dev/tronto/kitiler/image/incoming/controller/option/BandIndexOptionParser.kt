package dev.tronto.kitiler.image.incoming.controller.option

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.core.exception.RequiredParameterMissingException
import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OptionDescription
import dev.tronto.kitiler.core.incoming.controller.option.OptionParser
import dev.tronto.kitiler.core.incoming.controller.option.Request

class BandIndexOptionParser : OptionParser<BandIndexOption> {
    companion object {
        private const val PARAM = "bidx"
    }
    override val type: ArgumentType<BandIndexOption> = ArgumentType()

    override fun generateMissingException(): Exception = RequiredParameterMissingException(PARAM)

    override suspend fun parse(request: Request): BandIndexOption? {
        val indexes = request.parameter(PARAM).mapNotNull { it.toIntOrNull()?.let { BandIndex(it) } }
        return if (indexes.isNotEmpty()) BandIndexOption(indexes) else null
    }

    override fun box(option: BandIndexOption): Map<String, List<String>> = mapOf(
        PARAM to option.bandIndexes.map {
            it.value.toString()
        }
    )

    override fun descriptions(): List<OptionDescription<*>> =
        listOf(OptionDescription<IntArray>(PARAM, "band indexes", intArrayOf(1, 2, 3)))
}
