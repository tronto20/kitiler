package dev.tronto.kitiler.spring.autoconfigure.core

import dev.tronto.kitiler.core.outgoing.adaptor.gdal.GdalDatasetFactory
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.GdalRasterFactory
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.SpatialReferenceCRSFactory
import dev.tronto.kitiler.core.outgoing.port.CRSFactory
import dev.tronto.kitiler.core.outgoing.port.RasterFactory
import dev.tronto.kitiler.core.service.CoreService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@AutoConfiguration
@ComponentScan
@EnableConfigurationProperties(KitilerCorePathProperties::class)
class KitilerCoreAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CRSFactory::class)
    fun crsFactory(): CRSFactory = SpatialReferenceCRSFactory

    @Bean
    @ConditionalOnMissingBean(GdalDatasetFactory::class)
    fun gdalDatasetFactory(crsFactory: CRSFactory): GdalDatasetFactory = GdalDatasetFactory(crsFactory)

    @Bean
    @ConditionalOnBean(GdalDatasetFactory::class)
    @ConditionalOnMissingBean(RasterFactory::class)
    fun rasterFactory(crsFactory: CRSFactory, gdalDatasetFactory: GdalDatasetFactory): RasterFactory =
        GdalRasterFactory(crsFactory, gdalDatasetFactory)

    @Bean
    fun coreService(rasterFactory: RasterFactory): CoreService = CoreService(rasterFactory)
}
