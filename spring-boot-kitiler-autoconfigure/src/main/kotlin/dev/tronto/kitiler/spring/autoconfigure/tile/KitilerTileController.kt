package dev.tronto.kitiler.spring.autoconfigure.tile

import dev.tronto.kitiler.core.incoming.controller.option.filter
import dev.tronto.kitiler.image.incoming.usecase.ImageRenderUseCase
import dev.tronto.kitiler.spring.autoconfigure.webflux.GET
import dev.tronto.kitiler.spring.autoconfigure.webflux.WebFluxOptionParserAdaptor
import dev.tronto.kitiler.tile.incoming.usecase.TileInfoUseCase
import dev.tronto.kitiler.tile.incoming.usecase.TileMatrixSetUseCase
import dev.tronto.kitiler.tile.incoming.usecase.TileUseCase
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Controller
class KitilerTileController(
    private val optionParser: WebFluxOptionParserAdaptor,
    private val pathProperties: KitilerTilePathProperties,
    private val infoUseCase: TileInfoUseCase,
    private val tileUseCase: TileUseCase,
    private val renderUseCase: ImageRenderUseCase,
    private val tileMatrixSetUseCase: TileMatrixSetUseCase,
) : RouterFunction<ServerResponse> by coRouter({
    GET(pathProperties.info) {
        val options = optionParser.parse(it)
        val info = infoUseCase.tileInfo(options.filter(), options.filter())
        ok().bodyValueAndAwait(info)
    }

    GET(pathProperties.tiles) {
        val options = optionParser.parse(it)
        val tile = tileUseCase.tile(options.filter(), options.filter())
        val image = renderUseCase.renderImage(tile, options.filter())
        ok().bodyValueAndAwait(image)
    }

    GET(pathProperties.tileMatrixSets) {
        ok().bodyValueAndAwait(tileMatrixSetUseCase.tileMatrixSets())
    }

    GET(pathProperties.tileMatrixSet) {
        val options = optionParser.parse(it)
        ok().bodyValueAndAwait(tileMatrixSetUseCase.tileMatrixSet(options.filter()))
    }
})
