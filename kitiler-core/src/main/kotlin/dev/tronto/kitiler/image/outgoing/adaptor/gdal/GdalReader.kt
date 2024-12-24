package dev.tronto.kitiler.image.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.GdalDatasetFactory
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.gdalConst
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.handleError
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.Window
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import kotlin.reflect.KClass

internal class GdalReader(
    private val openOptions: OptionProvider<OpenOption>,
    private val gdalDatasetFactory: GdalDatasetFactory,
) {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }
    }

    suspend fun <T : Number> readData(
        kClass: KClass<T>,
        bandList: IntArray,
        width: Int,
        height: Int,
        window: Window,
    ): D3Array<T> = logger.logTrace("read gdal") {
        @Suppress("UNCHECKED_CAST")
        when (kClass) {
            Int::class -> readIntData(bandList, width, height, window)
            Long::class -> readLongData(bandList, width, height, window)
            Float::class -> readFloatData(bandList, width, height, window)
            Double::class -> readDoubleData(bandList, width, height, window)
            else -> throw UnsupportedOperationException("$kClass is not supported.")
        } as D3Array<T>
    }

    private suspend fun readIntData(bandList: IntArray, width: Int, height: Int, window: Window): D3Array<Int> {
        val arr = IntArray(bandList.size * width * height)
        gdalDatasetFactory.createGdalDataset(openOptions).use {
            val dataset = it.dataset
            dataset.handleError {
                ReadRaster(
                    window.xOffset,
                    window.yOffset,
                    window.width,
                    window.height,
                    width,
                    height,
                    DataType.Int32.gdalConst,
                    arr,
                    bandList
                )
            }
        }
        return mk.ndarray(arr, bandList.size, height, width)
    }

    private suspend fun readLongData(bandList: IntArray, width: Int, height: Int, window: Window): D3Array<Long> {
        val arr = LongArray(bandList.size * width * height)
        gdalDatasetFactory.createGdalDataset(openOptions).use {
            val dataset = it.dataset
            dataset.handleError {
                ReadRaster(
                    window.xOffset,
                    window.yOffset,
                    window.width,
                    window.height,
                    width,
                    height,
                    DataType.Int64.gdalConst,
                    arr,
                    bandList
                )
            }
        }
        return mk.ndarray(arr, bandList.size, height, width)
    }

    private suspend fun readFloatData(bandList: IntArray, width: Int, height: Int, window: Window): D3Array<Float> {
        val arr = FloatArray(bandList.size * width * height)
        gdalDatasetFactory.createGdalDataset(openOptions).use {
            val dataset = it.dataset
            dataset.handleError {
                ReadRaster(
                    window.xOffset,
                    window.yOffset,
                    window.width,
                    window.height,
                    width,
                    height,
                    DataType.Float32.gdalConst,
                    arr,
                    bandList
                )
            }
        }
        return mk.ndarray(arr, bandList.size, height, width)
    }

    private suspend fun readDoubleData(bandList: IntArray, width: Int, height: Int, window: Window): D3Array<Double> {
        val arr = DoubleArray(bandList.size * width * height)
        gdalDatasetFactory.createGdalDataset(openOptions).use {
            val dataset = it.dataset
            dataset.handleError {
                ReadRaster(
                    window.xOffset,
                    window.yOffset,
                    window.width,
                    window.height,
                    width,
                    height,
                    DataType.Float64.gdalConst,
                    arr,
                    bandList
                )
            }
        }
        return mk.ndarray(arr, bandList.size, height, width)
    }
}
