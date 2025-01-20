package dev.tronto.kitiler.image.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.gdalConst
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.use
import dev.tronto.kitiler.core.utils.logTrace
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gdal.gdal.Dataset
import org.gdal.gdal.Driver
import org.gdal.gdal.gdal

class GdalRenderer(
    driverName: String,
    private val width: Int,
    private val height: Int,
    private val band: Int,
    type: DataType,
    name: String,
) : AutoCloseable {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val driver: Driver = gdal.GetDriverByName(driverName)
    private val path: String = "/vsimem/$name"
    private val dataset: Dataset
    private val buffered: Boolean

    init {
        if (driver.canCreateCopy) {
            dataset = gdal.GetDriverByName("Mem")
                .Create(path, width, height, band, type.gdalConst)
            buffered = true
        } else if (driver.canCreate) {
            dataset = driver.Create(path, width, height, band, type.gdalConst)
            buffered = false
        } else {
            throw IllegalArgumentException("driver $driverName cannot create dataset.")
        }
    }

    fun write(data: IntArray, bands: IntArray) = logger.logTrace("GdalRenderer.write()") {
        require(bands.all { it in 1..band })
        dataset.WriteRaster(0, 0, width, height, width, height, DataType.Int32.gdalConst, data, bands)
    }

    fun toByteArray(): ByteArray = logger.logTrace("GdalRenderer.toByteArray()") {
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

/**
 *
 *  하나의 쓰레드에서 작업하는 것을 보장하기 위한 함수. [name] 이 같을 경우에 유용하게 사용할 수 있음.
 *  /vsimem 의 데이터셋 이름은 쓰레드별로 구분되기 때문에 쓰레드 구분이 필요
 */
fun render(
    driverName: String,
    width: Int,
    height: Int,
    band: Int,
    type: DataType,
    name: String,
    block: GdalRenderer.() -> Unit,
): ByteArray = GdalRenderer(
    driverName,
    width,
    height,
    band,
    type,
    name
).use {
    it.block()
    it.toByteArray()
}
