package dev.tronto.titiler.image.incoming.controller.option

import dev.tronto.titiler.core.exception.IllegalParameterException
import dev.tronto.titiler.core.exception.RequiredParameterMissingException
import dev.tronto.titiler.core.incoming.controller.option.ArgumentType
import dev.tronto.titiler.core.incoming.controller.option.OptionDescription
import dev.tronto.titiler.core.incoming.controller.option.OptionParser
import dev.tronto.titiler.core.incoming.controller.option.Request

class RescaleOptionParser : OptionParser<RescaleOption> {
    companion object {
        private const val PARAM = "rescale"
    }

    override val type: ArgumentType<RescaleOption> = ArgumentType()

    override fun generateMissingException(): Exception {
        return RequiredParameterMissingException(PARAM)
    }

    override suspend fun parse(request: Request): RescaleOption? {
        val rescales = request.parameter(PARAM).flatMap {
            it.split(',')
        }.map {
            val (min, max) = it.split(':').also {
                if (it.size != 2) {
                    throw IllegalParameterException("$PARAM must be shape of 'start:end'.")
                }
            }
            min.toDouble()..max.toDouble()
        }
        return if (rescales.isNotEmpty()) RescaleOption(rescales) else null
    }

    override fun box(option: RescaleOption): Map<String, List<String>> {
        return mapOf(PARAM to option.rescale.map { "${it.start}:${it.endInclusive}" })
    }

    override fun descriptions(): List<OptionDescription<*>> {
        return listOf(
            OptionDescription<Array<String>>(
                PARAM,
                "colon (':') delimited Min:Max range. Can set multiple time for multiple bands.",
                sample = arrayOf("10:90", "20:100", "15:95")
            )
        )
    }
}
