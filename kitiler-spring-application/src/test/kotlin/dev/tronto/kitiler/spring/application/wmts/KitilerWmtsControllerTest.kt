package dev.tronto.kitiler.spring.application.wmts

import com.ninjasquad.springmockk.MockkBean
import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionParser
import dev.tronto.kitiler.document.domain.Document
import dev.tronto.kitiler.document.domain.DocumentFormat
import dev.tronto.kitiler.image.incoming.controller.option.RenderOption
import dev.tronto.kitiler.spring.application.testAndDocument
import dev.tronto.kitiler.spring.autoconfigure.core.KitilerCoreAutoConfiguration
import dev.tronto.kitiler.spring.autoconfigure.utils.sortedByOrdered
import dev.tronto.kitiler.spring.autoconfigure.webflux.KitilerWebAutoConfiguration
import dev.tronto.kitiler.spring.autoconfigure.wmts.KitilerWmtsAutoConfiguration
import dev.tronto.kitiler.spring.autoconfigure.wmts.KitilerWmtsController
import dev.tronto.kitiler.spring.autoconfigure.wmts.KitilerWmtsPathProperties
import dev.tronto.kitiler.tile.incoming.controller.option.TileOption
import dev.tronto.kitiler.wmts.incoming.controller.option.WmtsOption
import dev.tronto.kitiler.wmts.incoming.usecase.WmtsUseCase
import io.kotest.core.spec.style.FeatureSpec
import io.mockk.coEvery
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.aot.DisabledInAotMode
import org.springframework.test.web.reactive.server.WebTestClient

@Import(KitilerWebAutoConfiguration::class, KitilerCoreAutoConfiguration::class, KitilerWmtsAutoConfiguration::class)
@DisabledInAotMode
@DisabledInNativeImage
@AutoConfigureRestDocs
@WebFluxTest(controllers = [KitilerWmtsController::class])
@MockkBean(WmtsUseCase::class)
class KitilerWmtsControllerTest(private val webTestClient: WebTestClient, private val optionParsers: ObjectProvider<OptionParser<*>>, private val pathProperties: KitilerWmtsPathProperties, private val wmtsUseCase: WmtsUseCase) :
    FeatureSpec({
        val parsers = optionParsers.sortedByOrdered()
        feature("wmts capabilities") {
            scenario("Get WMTSCapabilities") {
                coEvery { wmtsUseCase.wmts(any(), any(), any(), any()) } returns object : Document {
                    override val contents: String
                        get() = "contents"
                    override val format: DocumentFormat
                        get() = DocumentFormat.XML
                }

                webTestClient.testAndDocument(
                    "wmtsCapabilities",
                    "OGC WMTS endpoint.",
                    "Wmts",
                    "WMTSCapabilities",
                    pathProperties.capabilities,
                    parsers,
                    ArgumentType<OpenOption>(),
                    ArgumentType<TileOption>(),
                    ArgumentType<RenderOption>(),
                    ArgumentType<WmtsOption>()
                )
            }
        }
    })
