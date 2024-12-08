package dev.tronto.titiler.stat.service

import dev.tronto.titiler.core.domain.BandIndex
import dev.tronto.titiler.core.incoming.controller.option.OpenOption
import dev.tronto.titiler.core.incoming.controller.option.OptionProvider
import dev.tronto.titiler.core.incoming.controller.option.getOrNull
import dev.tronto.titiler.image.incoming.controller.option.BandIndexOption
import dev.tronto.titiler.image.incoming.controller.option.ImageOption
import dev.tronto.titiler.image.incoming.usecase.ImagePreviewUseCase
import dev.tronto.titiler.image.service.ImageService
import dev.tronto.titiler.stat.domain.Percentile
import dev.tronto.titiler.stat.domain.Statistics
import dev.tronto.titiler.stat.incoming.controller.option.PercentileOption
import dev.tronto.titiler.stat.incoming.controller.option.StatisticsOption
import dev.tronto.titiler.stat.incoming.usecase.StatisticsUseCase
import dev.tronto.titiler.stat.outgoing.port.spi.ImageDataStatistics
import java.util.*

class StatisticsService(
    private val previewUseCase: ImagePreviewUseCase = ImageService(),
    private val imageDataStatistics: List<ImageDataStatistics> = ServiceLoader.load(
        ImageDataStatistics::class.java,
        Thread.currentThread().contextClassLoader
    ).toList(),
) : StatisticsUseCase {
    override suspend fun statistics(
        openOptions: OptionProvider<OpenOption>,
        imageOptions: OptionProvider<ImageOption>,
        statisticsOptions: OptionProvider<StatisticsOption>,
    ): Statistics {
        val percentileOption: PercentileOption = statisticsOptions.getOrNull()
            ?: PercentileOption(listOf(Percentile(2), Percentile(98)))

        val preview = previewUseCase.preview(openOptions, imageOptions)
        val stat = imageDataStatistics.find {
            it.supports(preview)
        } ?: throw UnsupportedOperationException()
        val bandIndexOption: BandIndexOption? = imageOptions.getOrNull()
        val bandIndexes = bandIndexOption?.bandIndexes

        val bandStatisticsList = stat.statistics(preview, percentileOption.percentiles)
        val statistics = if (bandIndexes == null) {
            bandStatisticsList.mapIndexed { index, bandStatistics ->
                BandIndex(index + 1) to bandStatistics
            }
        } else {
            bandStatisticsList.mapIndexed { index, bandStatistics ->
                bandIndexes[index] to bandStatistics
            }
        }
        return Statistics(statistics.toMap())
    }
}
