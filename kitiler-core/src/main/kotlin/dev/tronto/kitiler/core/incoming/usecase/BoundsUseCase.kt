package dev.tronto.kitiler.core.incoming.usecase

import dev.tronto.kitiler.core.domain.Bounds
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider

interface BoundsUseCase {
    suspend fun getBounds(openOptions: OptionProvider<OpenOption>): Bounds
}
