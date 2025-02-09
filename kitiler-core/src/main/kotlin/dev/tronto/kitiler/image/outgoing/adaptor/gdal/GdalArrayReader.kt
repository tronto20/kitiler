package dev.tronto.kitiler.image.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.GdalDatasetFactory
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.gdalConst
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.handleError
import dev.tronto.kitiler.image.domain.Size
import dev.tronto.kitiler.image.domain.Window

internal class GdalArrayReader(
    private val openOptions: OptionProvider<OpenOption>,
    private val gdalDatasetFactory: GdalDatasetFactory,
) {
    suspend fun readBuffer(
        dataType: DataType,
        bandList: IntArray,
        rasterWindow: Window,
        bufferSize: Size,
        array: IntArray,
    ) {
        gdalDatasetFactory.createGdalDataset(openOptions).use {
            val dataset = it.dataset
            dataset.handleError {
                ReadRaster(
                    rasterWindow.xOffset,
                    rasterWindow.yOffset,
                    rasterWindow.width,
                    rasterWindow.height,
                    bufferSize.width,
                    bufferSize.height,
                    dataType.gdalConst,
                    array,
                    bandList
                )
            }
        }
    }

    suspend fun readBuffer(
        dataType: DataType,
        bandList: IntArray,
        rasterWindow: Window,
        bufferSize: Size,
        array: LongArray,
    ) {
        gdalDatasetFactory.createGdalDataset(openOptions).use {
            val dataset = it.dataset
            dataset.handleError {
                ReadRaster(
                    rasterWindow.xOffset,
                    rasterWindow.yOffset,
                    rasterWindow.width,
                    rasterWindow.height,
                    bufferSize.width,
                    bufferSize.height,
                    dataType.gdalConst,
                    array,
                    bandList
                )
            }
        }
    }

    suspend fun readBuffer(
        dataType: DataType,
        bandList: IntArray,
        rasterWindow: Window,
        bufferSize: Size,
        array: FloatArray,
    ) {
        gdalDatasetFactory.createGdalDataset(openOptions).use {
            val dataset = it.dataset
            dataset.handleError {
                ReadRaster(
                    rasterWindow.xOffset,
                    rasterWindow.yOffset,
                    rasterWindow.width,
                    rasterWindow.height,
                    bufferSize.width,
                    bufferSize.height,
                    dataType.gdalConst,
                    array,
                    bandList
                )
            }
        }
    }

    suspend fun readBuffer(
        dataType: DataType,
        bandList: IntArray,
        rasterWindow: Window,
        bufferSize: Size,
        array: DoubleArray,
    ) {
        gdalDatasetFactory.createGdalDataset(openOptions).use {
            val dataset = it.dataset
            dataset.handleError {
                ReadRaster(
                    rasterWindow.xOffset,
                    rasterWindow.yOffset,
                    rasterWindow.width,
                    rasterWindow.height,
                    bufferSize.width,
                    bufferSize.height,
                    dataType.gdalConst,
                    array,
                    bandList
                )
            }
        }
    }
}
