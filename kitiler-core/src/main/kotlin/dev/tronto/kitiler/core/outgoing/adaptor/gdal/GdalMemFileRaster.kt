package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gdal.gdal.gdal

class GdalMemFileRaster internal constructor(val gdalRaster: GdalRaster, val memFilePath: String) :
    GdalBaseRaster by gdalRaster {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }
    }

    override fun close() {
        gdalRaster.close()
        try {
            gdal.Unlink(memFilePath)
        } catch (e: RuntimeException) {
            logger.warn(e) { "error unlink memfile." }
            // ignore
        }
    }
}
