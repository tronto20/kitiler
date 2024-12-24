package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.outgoing.adaptor.jts.AffineCoordinateTransform
import dev.tronto.kitiler.core.outgoing.port.CRS
import dev.tronto.kitiler.core.outgoing.port.CRSFactory
import io.github.oshai.kotlinlogging.KotlinLogging
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
        val result = arrayOf<Double?>(null)
        sampleBand.GetNoDataValue(result)
        this.noDataValue = result[0]
    }

    override fun close() {
        kotlin.runCatching {
            sampleBand.delete()
        }
        kotlin.runCatching {
            dataset.delete()
        }
        memFilePath?.let { gdal.Unlink(it) }
    }

    fun getCrs(crsFactory: CRSFactory): CRS {
        // TODO :: GeoTransform 과 GCP 모두 없는 영상의 CRS 가 잘 만들어지는지 확인.
        val spatialRef = dataset.GetSpatialRef() ?: dataset.GetGCPSpatialRef()
        return try {
            crsFactory.create(spatialRef.ExportToWkt())
        } finally {
            try {
                spatialRef?.delete()
            } catch (e: Exception) {
                // ignore
                logger.warn(e) { "error in delete SpatialReference." }
            }
        }
    }

    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }
    }
}
