package dev.tronto.kitiler.tile.incoming.controller.option

import dev.tronto.kitiler.core.exception.IllegalParameterException
import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OptionDescription
import dev.tronto.kitiler.core.incoming.controller.option.OptionParser
import dev.tronto.kitiler.core.incoming.controller.option.Request

class TileMatrixSetOptionParser : OptionParser<TileMatrixSetOption> {
    companion object {
        private const val PARAM = "tileMatrixSetId"
        private const val DEFAULT = "WebMercatorQuad"
    }

    override val type: ArgumentType<TileMatrixSetOption> = ArgumentType()

    override fun generateMissingException(): Exception = IllegalParameterException(PARAM)

    override suspend fun parse(request: Request): TileMatrixSetOption? = request.parameter(PARAM).firstOrNull()?.let {
        TileMatrixSetOption(it)
    } ?: TileMatrixSetOption(DEFAULT)

    override fun box(option: TileMatrixSetOption): Map<String, List<String>> =
        mapOf(PARAM to listOf(option.tileMatrixSetId))

    override fun descriptions(): List<OptionDescription<*>> = listOf(
        OptionDescription<String>(
            PARAM,
            "TileMatrixSets, available values from `/tileMatrixSets`.",
            "WebMercatorQuad",
            default = DEFAULT
        )
    )
}
