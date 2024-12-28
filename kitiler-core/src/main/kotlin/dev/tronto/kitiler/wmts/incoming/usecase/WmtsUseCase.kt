package dev.tronto.kitiler.wmts.incoming.usecase

import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.document.domain.Document
import dev.tronto.kitiler.image.incoming.controller.option.RenderOption
import dev.tronto.kitiler.tile.incoming.controller.option.TileOption
import dev.tronto.kitiler.wmts.incoming.controller.option.WmtsOption

interface WmtsUseCase {
    suspend fun wmts(
        openOptions: OptionProvider<OpenOption>,
        renderOptions: OptionProvider<RenderOption>,
        tileOptions: OptionProvider<TileOption>,
        wmtsOptions: OptionProvider<WmtsOption>,
    ): Document
}
