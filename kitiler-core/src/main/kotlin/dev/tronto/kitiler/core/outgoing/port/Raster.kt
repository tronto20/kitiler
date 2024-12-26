package dev.tronto.kitiler.core.outgoing.port

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.core.domain.BandInfo
import dev.tronto.kitiler.core.domain.ColorInterpretation
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.domain.OptionContext
import org.locationtech.jts.geom.Envelope

interface Raster : OptionContext {
    val name: String
    val width: Int
    val height: Int
    val bandCount: Int
    val driver: String
    val dataType: DataType
    val noDataValue: Double?
    val noDataType: String
        get() = if (noDataValue != null) {
            "Nodata"
        } else if (hasAlphaBand()) {
            "Alpha"
        } else {
            "None"
        }
    val crs: CRS
    val pixelCoordinateTransform: CoordinateTransform

    fun bounds(): Envelope
    fun bandInfo(bandIndex: BandIndex): BandInfo

    fun hasAlphaBand(): Boolean = (1..this.bandCount).reversed().any {
        bandInfo(BandIndex(it)).colorInterpolation == ColorInterpretation.Alpha
    }
}
