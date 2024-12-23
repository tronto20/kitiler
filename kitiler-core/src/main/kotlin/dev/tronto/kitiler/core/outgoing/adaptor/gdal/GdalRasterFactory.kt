package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.core.domain.BandInfo
import dev.tronto.kitiler.core.domain.ColorInterpretation
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.outgoing.port.CRS
import dev.tronto.kitiler.core.outgoing.port.CRSFactory
import dev.tronto.kitiler.core.outgoing.port.RasterFactory
import io.github.oshai.kotlinlogging.KotlinLogging

open class GdalRasterFactory(
    private val crsFactory: CRSFactory = SpatialReferenceCRSFactory,
    private val gdalDatasetFactory: GdalDatasetFactory = GdalDatasetFactory(crsFactory),
) : RasterFactory {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }
    }

    private fun createGdalRaster(gdalDataset: GdalDataset, vararg options: OptionProvider<*>): GdalRaster {
        val dataset = gdalDataset.dataset
        val bandInfos = (1..gdalDataset.bandCount).map { band ->
            dataset.GetRasterBand(band).use {
                BandInfo(
                    BandIndex(band),
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

        // TODO :: GeoTransform 과 GCP 모두 없는 영상의 CRS 가 잘 만들어지는지 확인.
        val spatialRef = dataset.GetSpatialRef() ?: dataset.GetGCPSpatialRef()
        val crs: CRS = try {
            crsFactory.create(spatialRef.ExportToWkt())
        } finally {
            try {
                spatialRef?.delete()
            } catch (e: Exception) {
                // ignore
                logger.warn(e) { "error in delete SpatialReference." }
            }
        }

        return GdalRaster(
            gdalDataset.name,
            gdalDataset.width,
            gdalDataset.height,
            gdalDataset.bandCount,
            dataset.GetDriver().use { it.shortName },
            gdalDataset.dataType,
            gdalDataset.pixelCoordinateTransform,
            gdalDataset.noDataValue,
            crs,
            bandInfos,
            *options
        )
    }

    override suspend fun create(options: OptionProvider<OpenOption>): GdalRaster =
        gdalDatasetFactory.createGdalDataset(options).use {
            createGdalRaster(it, options)
        }
}
