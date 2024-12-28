package dev.tronto.kitiler.spring.autoconfigure.tile

import dev.tronto.kitiler.core.incoming.usecase.InfoUseCase
import dev.tronto.kitiler.core.outgoing.port.CRSFactory
import dev.tronto.kitiler.core.outgoing.port.RasterFactory
import dev.tronto.kitiler.image.incoming.usecase.ImageReadUseCase
import dev.tronto.kitiler.spring.autoconfigure.core.KitilerCoreAutoConfiguration
import dev.tronto.kitiler.spring.autoconfigure.image.KitilerImageAutoConfiguration
import dev.tronto.kitiler.tile.outgoing.adaptor.resource.ResourceTileMatrixSetFactory
import dev.tronto.kitiler.tile.outgoing.port.TileMatrixSetFactory
import dev.tronto.kitiler.tile.service.TileMatrixSetService
import dev.tronto.kitiler.tile.service.TileService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@ComponentScan
@AutoConfiguration(after = [KitilerCoreAutoConfiguration::class, KitilerImageAutoConfiguration::class])
@EnableConfigurationProperties(KitilerTileProperties::class, KitilerTilePathProperties::class)
class KitilerTileAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TileMatrixSetFactory::class)
    fun tileMatrixSetFactory(properties: KitilerTileProperties) =
        ResourceTileMatrixSetFactory(properties.tileMatrixSetResourcePattern)

    @Bean
    @ConditionalOnBean(CRSFactory::class, RasterFactory::class, ImageReadUseCase::class, InfoUseCase::class)
    fun tileService(
        tileMatrixSetFactory: TileMatrixSetFactory,
        crsFactory: CRSFactory,
        rasterFactory: RasterFactory,
        imageReadUseCase: ImageReadUseCase,
        infoUseCase: InfoUseCase,
    ) = TileService(
        tileMatrixSetFactory,
        crsFactory,
        rasterFactory,
        imageReadUseCase,
        infoUseCase
    )

    @Bean
    fun tileMatrixSetService(tileMatrixSetFactory: TileMatrixSetFactory) = TileMatrixSetService(
        tileMatrixSetFactory
    )
}
