package dev.tronto.kitiler.image.incoming.controller.option

import dev.tronto.kitiler.core.exception.IllegalParameterException
import dev.tronto.kitiler.core.exception.RequiredParameterMissingException
import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OptionDescription
import dev.tronto.kitiler.core.incoming.controller.option.OptionParser
import dev.tronto.kitiler.core.incoming.controller.option.Request

class ImageSizeOptionParser : OptionParser<ImageSizeOption> {
    companion object {
        private const val WIDTH = "width"
        private const val HEIGHT = "height"
    }
    override val type: ArgumentType<ImageSizeOption> = ArgumentType()

    override fun generateMissingException(): Exception = RequiredParameterMissingException(WIDTH, HEIGHT)

    override suspend fun parse(request: Request): ImageSizeOption? {
        val widthString = request.parameter(WIDTH).firstOrNull() ?: return null
        val heightString = request.parameter(HEIGHT).firstOrNull() ?: return null
        val width = widthString.toIntOrNull() ?: throw IllegalParameterException(
            "$WIDTH must be an integer: $widthString."
        )
        val height = heightString.toIntOrNull() ?: throw IllegalParameterException(
            "$HEIGHT must be an integer: $heightString."
        )
        return ImageSizeOption(width, height)
    }

    override fun box(option: ImageSizeOption): Map<String, List<String>> = mapOf(
        WIDTH to listOf(option.width.toString()),
        HEIGHT to listOf(option.height.toString())
    )

    override fun descriptions(): List<OptionDescription<*>> = listOf(
        OptionDescription<Int>(WIDTH, "image width", sample = 256),
        OptionDescription<Int>(HEIGHT, "image height", sample = 256)
    )
}
