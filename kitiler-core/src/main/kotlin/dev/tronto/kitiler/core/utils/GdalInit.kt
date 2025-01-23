package dev.tronto.kitiler.core.utils

import org.gdal.gdal.gdal
import org.gdal.ogr.ogr
import org.gdal.osr.osr

object GdalInit {
    init {
        gdal.AllRegister()
        gdal.UseExceptions()
        ogr.UseExceptions()
        osr.UseExceptions()
    }
}
