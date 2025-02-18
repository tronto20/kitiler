package dev.tronto.kitiler.core.service

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.core.domain.Bounds
import dev.tronto.kitiler.core.domain.Info
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.incoming.usecase.BoundsUseCase
import dev.tronto.kitiler.core.incoming.usecase.InfoUseCase
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.GdalRasterFactory
import dev.tronto.kitiler.core.outgoing.port.RasterFactory
import org.locationtech.jts.geom.Envelope

class CoreService(private val rasterFactory: RasterFactory = GdalRasterFactory()) :
    BoundsUseCase,
    InfoUseCase {
    private fun Envelope.toArray() = doubleArrayOf(minX, minY, maxX, maxY)

    override suspend fun getBounds(openOptions: OptionProvider<OpenOption>): Bounds {
        val raster = rasterFactory.create(openOptions)
        val envelope = raster.bounds()
        return Bounds(
            bounds = envelope.toArray()
        )
    }

    override suspend fun getInfo(openOptions: OptionProvider<OpenOption>): Info {
        val raster = rasterFactory.create(openOptions)
        return Info(
            raster.name,
            raster.bounds().toArray(),
            raster.dataType,
            raster.driver,
            raster.width,
            raster.height,
            raster.noDataType,
            raster.noDataValue,
            raster.bandCount,
            (1..raster.bandCount).map { raster.bandInfo(BandIndex(it)) }
        )
    }
}
