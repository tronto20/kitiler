package dev.tronto.kitiler.spring.autoconfigure.image

import dev.tronto.kitiler.core.outgoing.adaptor.gdal.GdalDatasetFactory
import dev.tronto.kitiler.core.outgoing.port.CRSFactory
import dev.tronto.kitiler.core.outgoing.port.RasterFactory
import dev.tronto.kitiler.image.outgoing.adaptor.gdal.GdalReadableRasterFactory
import dev.tronto.kitiler.image.outgoing.port.ReadableRasterFactory
import dev.tronto.kitiler.image.service.ImageService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@AutoConfiguration
@ComponentScan
@EnableConfigurationProperties(KitilerImagePathProperties::class)
class KitilerImageAutoConfiguration {

    @Bean
    @ConditionalOnBean(GdalDatasetFactory::class)
    @ConditionalOnMissingBean(ReadableRasterFactory::class)
    fun gdalReadableRasterFactory(
        crsFactory: CRSFactory,
        gdalDatasetFactory: GdalDatasetFactory,
        rasterFactory: RasterFactory,
    ): GdalReadableRasterFactory = GdalReadableRasterFactory(
        crsFactory,
        gdalDatasetFactory,
        rasterFactory
    )

    @Bean
    fun imageService(crsFactory: CRSFactory, readableRasterFactory: ReadableRasterFactory): ImageService = ImageService(
        crsFactory,
        readableRasterFactory
    )
}
