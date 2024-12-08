package dev.tronto.titiler.image.incoming.controller.option

import dev.tronto.titiler.core.exception.IllegalParameterException
import dev.tronto.titiler.core.exception.RequiredParameterMissingException
import dev.tronto.titiler.core.incoming.controller.option.ArgumentType
import dev.tronto.titiler.core.incoming.controller.option.OptionParser
import dev.tronto.titiler.core.incoming.controller.option.Request
import dev.tronto.titiler.image.domain.Window

class WindowOptionParser : OptionParser<WindowOption> {
    companion object {
        private const val MIN_X = "minx"
        private const val MIN_Y = "miny"
        private const val MAX_X = "maxx"
        private const val MAX_Y = "maxy"
    }

    override val type: ArgumentType<WindowOption> = ArgumentType()

    override fun generateMissingException(): Exception {
        return RequiredParameterMissingException(MIN_X, MAX_X, MIN_Y, MAX_Y)
    }

    override fun parse(request: Request): WindowOption? {
        val minxString = request.parameter(MIN_X).firstOrNull() ?: return null
        val maxxString = request.parameter(MAX_X).firstOrNull() ?: return null
        val minyString = request.parameter(MIN_Y).firstOrNull() ?: return null
        val maxyString = request.parameter(MAX_Y).firstOrNull() ?: return null
        val minx =
            minxString.toIntOrNull() ?: throw IllegalParameterException("$MIN_X must be an integer: $minxString.")
        val maxx =
            maxxString.toIntOrNull() ?: throw IllegalParameterException("$MAX_X must be an integer: $maxxString.")
        val miny =
            minyString.toIntOrNull() ?: throw IllegalParameterException("$MIN_Y must be an integer: $minyString.")
        val maxy =
            maxyString.toIntOrNull() ?: throw IllegalParameterException("$MAX_Y must be an integer: $maxyString.")

        return WindowOption(
            Window(
                minx,
                miny,
                maxx - minx,
                maxy - miny
            )
        )
    }

    override fun box(option: WindowOption): Map<String, List<String>> {
        return mapOf(
            MIN_X to listOf((option.window.xOffset).toString()),
            MIN_Y to listOf((option.window.yOffset).toString()),
            MAX_X to listOf((option.window.xOffset + option.window.width).toString()),
            MAX_Y to listOf((option.window.yOffset + option.window.height).toString())
        )
    }
}
