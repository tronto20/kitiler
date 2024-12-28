package dev.tronto.kitiler.stat.service

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.core.domain.OptionContext
import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.incoming.controller.option.getOrNull
import dev.tronto.kitiler.core.incoming.controller.option.plus
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.ImageFormat
import dev.tronto.kitiler.image.incoming.controller.option.BandIndexOption
import dev.tronto.kitiler.image.incoming.controller.option.FeatureOption
import dev.tronto.kitiler.image.incoming.controller.option.ImageOption
import dev.tronto.kitiler.image.incoming.controller.option.ImageSizeOption
import dev.tronto.kitiler.image.incoming.controller.option.MaxSizeOption
import dev.tronto.kitiler.image.incoming.controller.option.WindowOption
import dev.tronto.kitiler.image.outgoing.port.ImageDataAutoRescale
import dev.tronto.kitiler.stat.domain.Percentile
import dev.tronto.kitiler.stat.incoming.controller.option.PercentileOption
import dev.tronto.kitiler.stat.incoming.controller.option.StatisticsOption
import dev.tronto.kitiler.stat.incoming.usecase.StatisticsUseCase
import io.github.oshai.kotlinlogging.KotlinLogging

class StatisticsAutoRescale(private val statisticsUseCase: StatisticsUseCase = StatisticsService()) :
    ImageDataAutoRescale {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger {}
    }

    override fun supports(imageData: ImageData, format: ImageFormat): Boolean =
        (format == ImageFormat.PNG || format == ImageFormat.JPEG) && (imageData is OptionContext)

    override suspend fun rescale(imageData: ImageData, format: ImageFormat): ImageData = logger.logTrace("rescale") {
        require(supports(imageData, format) && imageData is OptionContext)
        val openOptions = imageData.getOptionProvider(ArgumentType<OpenOption>())
        val imageOptions = imageData.getOptionProvider(ArgumentType<ImageOption>())

        val filteredImageOptions = imageOptions
            .filterNot(ArgumentType<WindowOption>())
            .filterNot(ArgumentType<FeatureOption>())
            .filterNot(ArgumentType<MaxSizeOption>())
            .filterNot(ArgumentType<ImageSizeOption>())
            .plus(MaxSizeOption(1024))

        val percentileRange = Percentile(2)..Percentile(98)
        val statisticsOptions = OptionProvider.empty<StatisticsOption>() +
            PercentileOption(listOf(percentileRange.start, percentileRange.endInclusive))

        val statistics = logger.logTrace("rescale statistics") {
            statisticsUseCase.statistics(
                openOptions,
                filteredImageOptions,
                statisticsOptions
            )
        }

        val bandIndexOption: BandIndexOption? = openOptions.getOrNull()
        val bandIndexes = bandIndexOption?.bandIndexes ?: (1..imageData.band).map { BandIndex(it) }

        val rangeFrom = bandIndexes.map { bandIndex ->
            val percentiles = statistics.statistics.first { it.bandIndex == bandIndex }.percentiles
            val start = percentiles.first { it.percentile == percentileRange.start }.value
            val end = percentiles.first { it.percentile == percentileRange.endInclusive }.value
            start..end
        }
        return@logTrace logger.logTrace("do resclae") { imageData.rescaleToUInt8(rangeFrom) }
    }
}
