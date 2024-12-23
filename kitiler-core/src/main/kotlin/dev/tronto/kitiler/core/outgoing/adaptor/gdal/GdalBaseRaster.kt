package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.outgoing.port.Raster
import org.gdal.gdal.Dataset

interface GdalBaseRaster : Raster {
    val dataset: Dataset
}
