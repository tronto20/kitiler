package dev.tronto.kitiler.stat.service

import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.incoming.controller.option.getOrNull
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.incoming.controller.option.BandIndexOption
import dev.tronto.kitiler.image.incoming.controller.option.ImageOption
import dev.tronto.kitiler.image.incoming.usecase.ImagePreviewUseCase
import dev.tronto.kitiler.image.service.ImageService
import dev.tronto.kitiler.stat.domain.Percentile
import dev.tronto.kitiler.stat.domain.Statistics
import dev.tronto.kitiler.stat.incoming.controller.option.PercentileOption
import dev.tronto.kitiler.stat.incoming.controller.option.StatisticsOption
import dev.tronto.kitiler.stat.incoming.usecase.StatisticsUseCase
import dev.tronto.kitiler.stat.outgoing.port.spi.ImageDataStatistics
import io.github.oshai.kotlinlogging.KotlinLogging

class StatisticsService(
    private val previewUseCase: ImagePreviewUseCase = ImageService(),
    private val imageDataStatistics: List<ImageDataStatistics> = ImageDataStatistics.services,
) : StatisticsUseCase {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }
    }

    override suspend fun statistics(
        openOptions: OptionProvider<OpenOption>,
        imageOptions: OptionProvider<ImageOption>,
        statisticsOptions: OptionProvider<StatisticsOption>,
    ): Statistics {
        val percentileOption: PercentileOption = statisticsOptions.getOrNull()
            ?: PercentileOption(listOf(Percentile(2), Percentile(98)))

        val preview = logger.logTrace("stat preview") { previewUseCase.preview(openOptions, imageOptions) }
        val stat = imageDataStatistics.find {
            it.supports(preview)
        } ?: throw UnsupportedOperationException()
        val bandIndexOption: BandIndexOption? = openOptions.getOrNull()
        val bandIndexes = bandIndexOption?.bandIndexes

        val bandStatisticsList = stat.statistics(preview, percentileOption.percentiles)
        val statistics = if (bandIndexes == null) {
            bandStatisticsList
        } else {
            bandStatisticsList.mapIndexed { index, bandStatistics ->
                bandStatistics.copy(
                    bandIndex = bandIndexes[index]
                )
            }
        }
        return Statistics(statistics)
    }
}
