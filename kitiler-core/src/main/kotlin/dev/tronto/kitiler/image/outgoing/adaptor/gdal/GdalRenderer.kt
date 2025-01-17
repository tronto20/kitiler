package dev.tronto.kitiler.image.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.gdalConst
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.use
import org.gdal.gdal.Dataset
import org.gdal.gdal.Driver
import org.gdal.gdal.gdal
import java.util.UUID

class GdalRenderer(
    driverName: String,
    private val width: Int,
    private val height: Int,
    private val band: Int,
    type: DataType,
) : AutoCloseable {
    private val driver: Driver = gdal.GetDriverByName(driverName)
    private val path: String = "/vsimem/${UUID.randomUUID()}"
    private val dataset: Dataset
    private val buffered: Boolean

    init {
        if (driver.canCreate) {
            dataset = driver.Create(path, width, height, band, type.gdalConst)
            buffered = false
        } else if (driver.canCreateCopy) {
            dataset = gdal.GetDriverByName("Mem")
                .Create(path, width, height, band, type.gdalConst)
            buffered = true
        } else {
            throw IllegalArgumentException("driver $driverName cannot create dataset.")
        }
    }

    fun write(data: IntArray, bands: IntArray) {
        require(bands.all { it in 1..band })
        dataset.WriteRaster(0, 0, width, height, width, height, DataType.Int32.gdalConst, data, bands)
    }

    fun toByteArray(): ByteArray {
        dataset.FlushCache()
        return if (buffered) {
            val tmpPath = "$path.tmp"
            driver.CreateCopy(tmpPath, dataset).use {
                gdal.GetMemFileBuffer(tmpPath)
            }.also {
                gdal.Unlink(tmpPath)
            }
        } else {
            gdal.GetMemFileBuffer(path)
        }
    }

    override fun close() {
        dataset.delete()
        gdal.Unlink(path)
    }
}
