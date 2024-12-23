package dev.tronto.kitiler.core.outgoing.port

import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider

interface RasterFactory {
    suspend fun <T> withRaster(openOptions: OptionProvider<OpenOption>, block: (dataset: Raster) -> T): T
}
