package dev.tronto.kitiler.image.outgoing.port

import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.outgoing.port.RasterFactory

interface ReadableRasterFactory : RasterFactory {
    suspend fun <T> withReadableRaster(
        openOptions: OptionProvider<OpenOption>,
        block: (dataset: ReadableRaster) -> T,
    ): T
}
