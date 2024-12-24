package dev.tronto.kitiler.core.outgoing.port

import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider

interface RasterFactory {
    suspend fun create(options: OptionProvider<OpenOption>): Raster
}
