package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.outgoing.adaptor.jts.AffineCoordinateTransform
import org.gdal.gdal.Band
import org.gdal.gdal.Dataset
import org.gdal.gdal.gdal
import org.locationtech.jts.geom.util.AffineTransformation

class GdalDataset(val name: String, val dataset: Dataset, private val memFilePath: String? = null) : AutoCloseable {
    val width: Int = dataset.rasterXSize
    val height: Int = dataset.rasterYSize
    val bandCount: Int = dataset.GetRasterCount()
    val sampleBand: Band = dataset.GetRasterBand(1)
    val dataType: DataType = DataType[sampleBand.GetRasterDataType()]
    val noDataValue: Double?
    val geoTransform = dataset.GetGeoTransform()
    val pixelCoordinateTransform = AffineCoordinateTransform(
        AffineTransformation(
            geoTransform[1],
            geoTransform[2],
            geoTransform[0],
            geoTransform[4],
            geoTransform[5],
            geoTransform[3]
        ).inverse
    )

    init {
        val result = Array<Double?>(1) { null }
        sampleBand.GetNoDataValue(result)
        this.noDataValue = result[0]
    }

    override fun close() {
        sampleBand.delete()
        dataset.delete()
        memFilePath?.let { gdal.Unlink(it) }
    }
}
