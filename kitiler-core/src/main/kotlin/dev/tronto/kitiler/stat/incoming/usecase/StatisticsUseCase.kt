package dev.tronto.kitiler.stat.incoming.usecase

import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.image.incoming.controller.option.ImageOption
import dev.tronto.kitiler.stat.domain.Statistics
import dev.tronto.kitiler.stat.incoming.controller.option.StatisticsOption

interface StatisticsUseCase {
    suspend fun statistics(
        openOptions: OptionProvider<OpenOption>,
        imageOptions: OptionProvider<ImageOption>,
        statisticsOptions: OptionProvider<StatisticsOption>,
    ): Statistics
}
