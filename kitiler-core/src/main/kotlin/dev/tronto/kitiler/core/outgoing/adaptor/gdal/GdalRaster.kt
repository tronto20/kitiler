package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.core.domain.BandInfo
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.domain.OptionContext
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.outgoing.port.CRS
import dev.tronto.kitiler.core.outgoing.port.CoordinateTransform
import dev.tronto.kitiler.core.outgoing.port.Raster
import org.locationtech.jts.geom.CoordinateXY
import org.locationtech.jts.geom.Envelope

class GdalRaster(
    override val name: String,
    override val width: Int,
    override val height: Int,
    override val bandCount: Int,
    override val driver: String,
    override val dataType: DataType,
    override val pixelCoordinateTransform: CoordinateTransform,
    override val noDataValue: Double?,
    override val crs: CRS,
    private val bandInfos: List<BandInfo>,
    vararg optionProviders: OptionProvider<*>,
) : Raster,
    OptionContext by OptionContext.wrap(*optionProviders) {
    override fun bounds(): Envelope {
        val upperLeft = CoordinateXY(0.0, 0.0)
        val lowerRight = CoordinateXY(width.toDouble(), height.toDouble())
        return Envelope(
            pixelCoordinateTransform.inverse(upperLeft),
            pixelCoordinateTransform.inverse(lowerRight)
        )
    }

    override fun bandInfo(bandIndex: BandIndex): BandInfo = bandInfos[bandIndex.value - 1]
}
