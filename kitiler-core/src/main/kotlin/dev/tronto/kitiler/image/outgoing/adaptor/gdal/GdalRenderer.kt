package dev.tronto.kitiler.image.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.ColorInterpretation
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.gdalConst
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.handleError
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.use
import dev.tronto.kitiler.core.utils.GdalInit
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.DataBuffer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gdal.gdal.Dataset
import org.gdal.gdal.Driver
import org.gdal.gdal.gdal
import java.util.*

class GdalRenderer private constructor(
    driverName: String,
    private val width: Int,
    private val height: Int,
    private val band: Int,
    type: DataType,
    name: String,
) {

    init {
        GdalInit
    }

    /**
     *  /vsimem 은 같은 경로에 대해 하나의 프로세스에서(모든 쓰레드에서) 접근할 수 있기에 UUID 로 경로를 구분함.
     */
    private val path: String = "/vsimem/${UUID.randomUUID()}/$name"

    private val dataset: Dataset
    private val buffered: Boolean
    private val driver: Driver = gdal.GetDriverByName(driverName)

    init {
        when {
            driver.canCreate -> {
                dataset = driver.Create(path, width, height, band, type.gdalConst)
                buffered = false
            }

            driver.canCreateCopy -> {
                dataset = memDriver.Create(path, width, height, band, type.gdalConst)
                buffered = true
            }

            else -> throw IllegalArgumentException("driver $driverName cannot create dataset.")
        }
    }

    fun write(data: IntArray, bands: IntArray) = logger.logTrace("GdalRenderer.write()") {
        require(bands.all { it in 1..band })
        dataset.handleError {
            WriteRaster(0, 0, width, height, width, height, DataType.Int32.gdalConst, data, bands)
        }
    }

    fun write(data: DataBuffer, bands: IntArray) = logger.logTrace("GdalRenderer.write()") {
        require(bands.all { it in 1..band })
        when {
            data.isIntArray -> {
                dataset.handleError {
                    WriteRaster(
                        0, 0, width, height, width, height,
                        data.dataType.gdalConst, data.intArray, bands
                    )
                }
            }

            data.isLongArray -> {
                dataset.handleError {
                    WriteRaster(
                        0, 0, width, height, width, height,
                        data.dataType.gdalConst, data.longArray, bands
                    )
                }
            }

            data.isFloatArray -> {
                dataset.handleError {
                    WriteRaster(
                        0, 0, width, height, width, height,
                        data.dataType.gdalConst, data.floatArray, bands
                    )
                }
            }

            data.isDoubleArray -> {
                dataset.handleError {
                    WriteRaster(
                        0, 0, width, height, width, height,
                        data.dataType.gdalConst, data.doubleArray, bands
                    )
                }
            }

            else -> UnsupportedOperationException()
        }
    }

    fun setColorInterpretation(band: Int, colorInterpretation: ColorInterpretation) {
        val rasterBand = dataset.GetRasterBand(band)
        rasterBand.SetColorInterpretation(colorInterpretation.gdalConst)
    }

    fun flush(): ByteArray = logger.logTrace("GdalRenderer.flush()") {
        dataset.FlushCache()
        val result = if (buffered) {
            val tmpPath = "$path.tmp"
            driver.CreateCopy(tmpPath, dataset).use {
                it.FlushCache()
                gdal.GetMemFileBuffer(tmpPath)
            }.also {
                gdal.Unlink(tmpPath)
            }
        } else {
            gdal.GetMemFileBuffer(path)
        }
        dataset.delete()
        gdal.Unlink(path)
        return result
    }

    companion object {
        private val logger = KotlinLogging.logger { }
        private val memDriver by lazy {
            gdal.GetDriverByName("Mem")!!
        }

        /**
         *  하나의 쓰레드에서 작업하는 것을 보장하기 위해 사용.
         */
        fun render(
            driverName: String,
            width: Int,
            height: Int,
            bandCount: Int,
            type: DataType,
            name: String,
            block: GdalRenderer.() -> Unit,
        ): ByteArray = GdalRenderer(
            driverName,
            width,
            height,
            bandCount,
            type,
            name
        ).let {
            it.block()
            it.flush()
        }

        fun render(
            driverName: String,
            width: Int,
            height: Int,
            bandCount: Int,
            type: DataType,
            name: String,
            dataBuffer: DataBuffer,
            validBuffer: DataBuffer?,
        ): ByteArray = GdalRenderer(
            driverName,
            width,
            height,
            if (validBuffer != null) bandCount + 1 else bandCount,
            type,
            name
        ).let { renderer ->
            renderer.write(dataBuffer, IntArray(bandCount) { it + 1 })
            validBuffer?.let {
                val alphaBand = bandCount + 1
                renderer.setColorInterpretation(alphaBand, ColorInterpretation.Alpha)
                renderer.write(it, intArrayOf(alphaBand))
            }
            renderer.flush()
        }
    }
}
