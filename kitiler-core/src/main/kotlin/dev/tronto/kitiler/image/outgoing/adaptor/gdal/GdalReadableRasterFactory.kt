package dev.tronto.kitiler.image.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.GdalRasterFactory
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.SpatialReferenceCRSFactory
import dev.tronto.kitiler.core.outgoing.port.CRSFactory
import dev.tronto.kitiler.core.outgoing.port.RasterFactory
import dev.tronto.kitiler.image.outgoing.port.ReadableRaster
import dev.tronto.kitiler.image.outgoing.port.ReadableRasterFactory

open class GdalReadableRasterFactory(
    private val crsFactory: CRSFactory = SpatialReferenceCRSFactory,
    private val gdalRasterFactory: GdalRasterFactory = GdalRasterFactory(crsFactory),
) : ReadableRasterFactory,
    RasterFactory by gdalRasterFactory {
    override suspend fun <T> withReadableRaster(
        openOptions: OptionProvider<OpenOption>,
        block: (dataset: ReadableRaster) -> T,
    ): T = gdalRasterFactory.withGdalRaster(openOptions) { raster ->
        GdalReadableRaster(raster).use(block)
    }
}
