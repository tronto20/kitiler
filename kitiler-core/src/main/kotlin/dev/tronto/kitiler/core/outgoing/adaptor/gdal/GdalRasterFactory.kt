package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.core.domain.BandInfo
import dev.tronto.kitiler.core.domain.ColorInterpretation
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
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
        val bandInfos = (1..gdalDataset.bandCount).map { bandIndex ->
            val band = dataset.GetRasterBand(bandIndex)
            BandInfo(
                BandIndex(bandIndex),
                DataType[band.dataType],
                band.GetDescription() ?: "",
                ColorInterpretation[band.GetColorInterpretation()],
                band.GetMetadata_Dict().mapNotNull {
                    val key = it.key as? String ?: return@mapNotNull null
                    val value = it.value as? String ?: return@mapNotNull null
                    key to value
                }.toMap()
            )
        }

        val crs = gdalDataset.getCrs(crsFactory)

        return GdalRaster(
            gdalDataset.name,
            gdalDataset.width,
            gdalDataset.height,
            gdalDataset.bandCount,
            dataset.GetDriver().shortName,
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
