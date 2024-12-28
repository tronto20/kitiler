package dev.tronto.kitiler.spring.autoconfigure.wmts

import dev.tronto.kitiler.core.outgoing.port.CRSFactory
import dev.tronto.kitiler.core.utils.TemplateString
import dev.tronto.kitiler.spring.autoconfigure.document.KitilerDocumentAutoConfiguration
import dev.tronto.kitiler.spring.autoconfigure.tile.KitilerTileAutoConfiguration
import dev.tronto.kitiler.spring.autoconfigure.tile.KitilerTilePathProperties
import dev.tronto.kitiler.spring.autoconfigure.webflux.KitilerWebProperties
import dev.tronto.kitiler.tile.incoming.usecase.TileInfoUseCase
import dev.tronto.kitiler.tile.outgoing.port.TileMatrixSetFactory
import dev.tronto.kitiler.wmts.service.WmtsService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.thymeleaf.TemplateEngine

@ComponentScan
@AutoConfiguration(after = [KitilerDocumentAutoConfiguration::class, KitilerTileAutoConfiguration::class])
@EnableConfigurationProperties(KitilerWmtsPathProperties::class)
class KitilerWmtsAutoConfiguration {

    @Bean
    @ConditionalOnBean(
        TemplateEngine::class,
        CRSFactory::class,
        TileMatrixSetFactory::class,
        TileInfoUseCase::class,
        KitilerTilePathProperties::class
    )
    fun wmtsService(
        templateEngine: TemplateEngine,
        crsFactory: CRSFactory,
        tileMatrixSetFactory: TileMatrixSetFactory,
        tileInfoUseCase: TileInfoUseCase,
        webProperties: KitilerWebProperties,
        tilePathProperties: KitilerTilePathProperties,
        wmtsPathProperties: KitilerWmtsPathProperties,
    ) = WmtsService(
        wmtsUriTemplate = TemplateString(
            webProperties.baseUri.trimEnd('/') + '/' +
                wmtsPathProperties.capabilities.first().trimStart('/')
        ),
        tilesUriTemplate = TemplateString(
            webProperties.baseUri.trimEnd('/') + '/' +
                tilePathProperties.tiles.first().trimStart('/')
        ),
        templateEngine = templateEngine,
        crsFactory = crsFactory,
        tileMatrixSetFactory = tileMatrixSetFactory,
        tileInfoUseCase = tileInfoUseCase
    )
}
