package dev.tronto.kitiler.spring.application.tile

import com.ninjasquad.springmockk.MockkBean
import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionParser
import dev.tronto.kitiler.image.incoming.controller.option.RenderOption
import dev.tronto.kitiler.image.incoming.usecase.ImageRenderUseCase
import dev.tronto.kitiler.spring.application.image.testImage
import dev.tronto.kitiler.spring.application.image.testImageData
import dev.tronto.kitiler.spring.application.testAndDocument
import dev.tronto.kitiler.spring.autoconfigure.core.KitilerCoreAutoConfiguration
import dev.tronto.kitiler.spring.autoconfigure.image.KitilerImageRenderAutoConfiguration
import dev.tronto.kitiler.spring.autoconfigure.tile.KitilerTileAutoConfiguration
import dev.tronto.kitiler.spring.autoconfigure.tile.KitilerTileController
import dev.tronto.kitiler.spring.autoconfigure.tile.KitilerTilePathProperties
import dev.tronto.kitiler.spring.autoconfigure.utils.sortedByOrdered
import dev.tronto.kitiler.spring.autoconfigure.webflux.KitilerWebAutoConfiguration
import dev.tronto.kitiler.tile.incoming.controller.option.TileMatrixSetOption
import dev.tronto.kitiler.tile.incoming.controller.option.TileOption
import dev.tronto.kitiler.tile.incoming.usecase.TileInfoUseCase
import dev.tronto.kitiler.tile.incoming.usecase.TileMatrixSetUseCase
import dev.tronto.kitiler.tile.incoming.usecase.TileUseCase
import io.kotest.core.spec.style.FeatureSpec
import io.mockk.coEvery
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.aot.DisabledInAotMode
import org.springframework.test.web.reactive.server.WebTestClient

@Import(
    KitilerWebAutoConfiguration::class,
    KitilerCoreAutoConfiguration::class,
    KitilerTileAutoConfiguration::class,
    KitilerImageRenderAutoConfiguration::class
)
@DisabledInAotMode
@DisabledInNativeImage
@AutoConfigureRestDocs
@WebFluxTest(controllers = [KitilerTileController::class])
@MockkBean(TileInfoUseCase::class, TileUseCase::class, ImageRenderUseCase::class, TileMatrixSetUseCase::class)
class KitilerTileControllerTest(
    private val webTestClient: WebTestClient,
    private val optionParsers: ObjectProvider<OptionParser<*>>,
    private val pathProperties: KitilerTilePathProperties,
    private val infoUseCase: TileInfoUseCase,
    private val tileUseCase: TileUseCase,
    private val renderUseCase: ImageRenderUseCase,
    private val tileMatrixSetUseCase: TileMatrixSetUseCase,
) : FeatureSpec({
    val parsers = optionParsers.sortedByOrdered()

    feature("tile info") {
        scenario("Get TileInfo") {
            coEvery { infoUseCase.tileInfo(any(), any()) } returns testTileInfo

            webTestClient.testAndDocument(
                "tileInfo",
                "Return dataset's tile info.",
                "TileInfo",
                "TileInfo",
                pathProperties.info,
                parsers,
                ArgumentType<OpenOption>(),
                ArgumentType<TileOption>(),
                responseFields = tileInfoFields
            )
        }
    }

    feature("tile") {
        scenario("Get Tile") {
            coEvery { tileUseCase.tile(any(), any()) } returns testImageData
            coEvery { renderUseCase.renderImage(testImageData, any()) } returns testImage

            webTestClient.testAndDocument(
                "tile",
                "Create map tile from a dataset.",
                "Tile",
                "Image",
                pathProperties.tiles,
                parsers,
                ArgumentType<OpenOption>(),
                ArgumentType<TileOption>(),
                ArgumentType<RenderOption>()
            )
        }
    }

    feature("tileMatrixSet") {
        scenario("List TileMatrixSets") {
            coEvery { tileMatrixSetUseCase.tileMatrixSets() } returns listOf(testTileMatrixSet)

            webTestClient.testAndDocument(
                "tileMatrixSets",
                "list TileMatrixSet.",
                "TileMatrixSet",
                "TileMatrixSetList",
                pathProperties.tileMatrixSets,
                parsers,
                tags = listOf("tileMatrixSet")
            )
        }

        scenario("Get TileMatrixSet") {
            coEvery { tileMatrixSetUseCase.tileMatrixSet(any()) } returns testTileMatrixSet

            webTestClient.testAndDocument(
                "tileMatrixSet",
                "Get TileMatrixSet by id.",
                "TileMatrixSet",
                "TileMatrixSet",
                pathProperties.tileMatrixSet,
                parsers,
                ArgumentType<TileMatrixSetOption>()
            )
        }
    }
})
