package dev.tronto.kitiler.image.incoming.controller.option

import dev.tronto.kitiler.core.exception.IllegalParameterException
import dev.tronto.kitiler.core.exception.RequiredParameterMissingException
import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OptionDescription
import dev.tronto.kitiler.core.incoming.controller.option.OptionParser
import dev.tronto.kitiler.core.incoming.controller.option.Request
import dev.tronto.kitiler.image.domain.ImageFormat
import dev.tronto.kitiler.image.spi.ImageFormatRegistrar

class ImageFormatOptionParser(imageFormats: Iterable<ImageFormat>) : OptionParser<ImageFormatOption> {

    constructor() : this(
        ImageFormatRegistrar.services.flatMap { it.imageFormats() }
    )

    companion object {
        private const val CONTENT_TYPE = "content-type"
        private const val PARAM = "format"
    }

    private val imageFormatMap = imageFormats
        .flatMap { format -> (listOf(format.name, *format.aliasNames.toTypedArray())).map { it to format } }
        .toMap()

    override val type: ArgumentType<ImageFormatOption> = ArgumentType()

    override fun generateMissingException(): Exception = RequiredParameterMissingException(PARAM)

    override suspend fun parse(request: Request): ImageFormatOption? {
        request.option(CONTENT_TYPE).firstOrNull()?.let {
            return createOption(CONTENT_TYPE, it)
        }
        request.parameter(PARAM).firstOrNull()?.let {
            return createOption(PARAM, it)
        }
        return null
    }

    private fun createOption(name: String, value: String): ImageFormatOption {
        try {
            val format = imageFormatMap[value]
                ?: throw IllegalParameterException("$name must be one of ${imageFormatMap.keys}")
            return ImageFormatOption(format)
        } catch (e: IllegalArgumentException) {
            throw IllegalParameterException(e.message, e)
        }
    }

    override fun box(option: ImageFormatOption): Map<String, List<String>> = mapOf(PARAM to listOf(option.format.name))

    override fun descriptions(): List<OptionDescription<*>> = listOf(
        OptionDescription<String>(
            PARAM,
            "image formats.",
            ImageFormat.PNG.name,
            enums = imageFormatMap.keys
        )
    )
}
