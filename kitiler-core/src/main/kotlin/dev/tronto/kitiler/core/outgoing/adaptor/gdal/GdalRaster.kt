package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.core.domain.BandInfo
import dev.tronto.kitiler.core.domain.ColorInterpretation
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.outgoing.adaptor.jts.AffineCoordinateTransform
import dev.tronto.kitiler.core.outgoing.port.CRS
import dev.tronto.kitiler.core.outgoing.port.CRSFactory
import dev.tronto.kitiler.core.outgoing.port.CoordinateTransform
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gdal.gdal.Band
import org.gdal.gdal.Dataset
import org.locationtech.jts.geom.CoordinateXY
import org.locationtech.jts.geom.Envelope
import org.locationtech.jts.geom.util.AffineTransformation

class GdalRaster internal constructor(
    override val dataset: Dataset,
    val crsFactory: CRSFactory,
    override val name: String,
) : GdalBaseRaster {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }
    }

    // for sample
    private val sampleBand: Band = dataset.GetRasterBand(1)

    val geoTransform: DoubleArray = dataset.GetGeoTransform()
    override val width = dataset.rasterXSize
    override val height = dataset.rasterYSize
    override val bandCount: Int = dataset.GetRasterCount()
    override val driver: String = dataset.GetDriver().use { it.shortName }
    override val dataType: DataType = DataType[sampleBand.GetRasterDataType()]

    // TODO :: GeoTransform 과 GCP 모두 없는 영상의 CRS 가 잘 만들어지는지 확인.
    override val crs: CRS by lazy {
        val spatialRef = dataset.GetSpatialRef() ?: dataset.GetGCPSpatialRef()
        try {
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

    override val noDataType: String by lazy {
        if (noDataValue != null) {
            "Nodata"
        } else if (hasAlphaBand()) {
            "Alpha"
        } else {
            "None"
        }
    }

    override val noDataValue: Double? by lazy {
        val result = Array<Double?>(1) { null }
        sampleBand.GetNoDataValue(result)
        result[0]
    }

    override val pixelCoordinateTransform: CoordinateTransform by lazy {
        AffineCoordinateTransform(
            AffineTransformation(
                geoTransform[1],
                geoTransform[2],
                geoTransform[0],
                geoTransform[4],
                geoTransform[5],
                geoTransform[3]
            ).inverse
        )
    }

    override fun close() {
        sampleBand.delete()
    }

    override fun bounds(): Envelope {
        val upperLeft = CoordinateXY(0.0, 0.0)
        val lowerRight = CoordinateXY(width.toDouble(), height.toDouble())
        return Envelope(
            pixelCoordinateTransform.inverse(upperLeft),
            pixelCoordinateTransform.inverse(lowerRight)
        )
    }

    override fun bandInfo(bandIndex: BandIndex): BandInfo {
        return dataset.GetRasterBand(bandIndex.value).use {
            BandInfo(
                bandIndex,
                DataType[it.dataType],
                it.GetDescription() ?: "",
                ColorInterpretation[it.GetColorInterpretation()],
                it.GetMetadata_Dict().mapNotNull {
                    val key = it.key as? String ?: return@mapNotNull null
                    val value = it.value as? String ?: return@mapNotNull null
                    key to value
                }.toMap()
            )
        }
    }
}
