package dev.tronto.kitiler.spring.application.stat

import com.ninjasquad.springmockk.MockkBean
import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionParser
import dev.tronto.kitiler.image.incoming.controller.option.ImageOption
import dev.tronto.kitiler.spring.application.testAndDocument
import dev.tronto.kitiler.spring.autoconfigure.core.KitilerCoreAutoConfiguration
import dev.tronto.kitiler.spring.autoconfigure.stat.KitilerStatAutoConfiguration
import dev.tronto.kitiler.spring.autoconfigure.stat.KitilerStatController
import dev.tronto.kitiler.spring.autoconfigure.stat.KitilerStatPathProperties
import dev.tronto.kitiler.spring.autoconfigure.utils.sortedByOrdered
import dev.tronto.kitiler.spring.autoconfigure.webflux.KitilerWebAutoConfiguration
import dev.tronto.kitiler.stat.incoming.controller.option.StatisticsOption
import dev.tronto.kitiler.stat.incoming.usecase.StatisticsUseCase
import io.kotest.core.spec.style.FeatureSpec
import io.mockk.coEvery
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.aot.DisabledInAotMode
import org.springframework.test.web.reactive.server.WebTestClient

@Import(KitilerWebAutoConfiguration::class, KitilerCoreAutoConfiguration::class, KitilerStatAutoConfiguration::class)
@DisabledInAotMode
@DisabledInNativeImage
@AutoConfigureRestDocs
@WebFluxTest(controllers = [KitilerStatController::class])
@MockkBean(StatisticsUseCase::class)
class KitilerStatControllerTest(private val webTestClient: WebTestClient, private val optionParsers: ObjectProvider<OptionParser<*>>, private val pathProperties: KitilerStatPathProperties, private val statisticsUseCase: StatisticsUseCase) :
    FeatureSpec({
        val parsers = optionParsers.sortedByOrdered()
        feature("statistics") {
            scenario("Get Statistics") {

                coEvery { statisticsUseCase.statistics(any(), any(), any()) } returns testStatistics

                webTestClient.testAndDocument(
                    "statistics",
                    "Get Dataset statistics.",
                    "Statistics",
                    "Statistics",
                    pathProperties.statistics,
                    parsers,
                    ArgumentType<OpenOption>(),
                    ArgumentType<ImageOption>(),
                    ArgumentType<StatisticsOption>(),
                    responseFields = statisticsFields
                )
            }
        }
    })
