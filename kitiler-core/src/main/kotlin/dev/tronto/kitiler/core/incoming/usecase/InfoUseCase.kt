package dev.tronto.kitiler.core.incoming.usecase

import dev.tronto.kitiler.core.domain.Info
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider

interface InfoUseCase {
    suspend fun getInfo(openOptions: OptionProvider<OpenOption>): Info
}
