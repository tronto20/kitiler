package dev.tronto.kitiler.tile.incoming.controller.option

import dev.tronto.kitiler.core.exception.IllegalParameterException
import dev.tronto.kitiler.core.exception.RequiredParameterMissingException
import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OptionDescription
import dev.tronto.kitiler.core.incoming.controller.option.OptionParser
import dev.tronto.kitiler.core.incoming.controller.option.Request

class TileScaleOptionParser : OptionParser<TileScaleOption> {
    companion object {
        private const val PARAM = "scale"
    }

    override val type: ArgumentType<TileScaleOption> = ArgumentType()

    override fun generateMissingException(): Exception = RequiredParameterMissingException(PARAM)

    override suspend fun parse(request: Request): TileScaleOption? = request.parameter(PARAM).firstOrNull()?.let {
        val value = it.toIntOrNull() ?: throw IllegalParameterException("scale must be an integer: $it.")
        TileScaleOption(value)
    }

    override fun box(option: TileScaleOption): Map<String, List<String>> =
        mapOf(PARAM to listOf(option.scale.toString()))

    override fun descriptions(): List<OptionDescription<*>> =
        listOf(OptionDescription<Int>(PARAM, "Tile Scale", 1, enums = listOf(1, 2, 3, 4)))
}
