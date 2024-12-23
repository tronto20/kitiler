package dev.tronto.kitiler.image.incoming.controller.option

import dev.tronto.kitiler.core.exception.IllegalParameterException
import dev.tronto.kitiler.core.exception.RequiredParameterMissingException
import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OptionDescription
import dev.tronto.kitiler.core.incoming.controller.option.OptionParser
import dev.tronto.kitiler.core.incoming.controller.option.Request

class FeatureOptionParser : OptionParser<FeatureOption> {
    companion object {
        private const val DEFAULT_CRS = "EPSG:4326"
        private const val PARAM = "feature"
    }

    override val type: ArgumentType<FeatureOption> = ArgumentType()

    override fun generateMissingException(): Exception = RequiredParameterMissingException(PARAM)

    override suspend fun parse(request: Request): FeatureOption? = request.parameter(PARAM).firstOrNull()?.let {
        val crs = request.parameter("featureCrs").firstOrNull()
//            FeatureOption(it, crs ?: DEFAULT_CRS)
        throw NotImplementedError("not implemented yet.")
    }

    override fun box(option: FeatureOption): Nothing = throw IllegalParameterException("$PARAM is not supported.")

    override fun descriptions(): List<OptionDescription<*>> {
        // TODO
        return emptyList()
    }
}
