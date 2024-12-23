package dev.tronto.kitiler.image.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.core.domain.ColorInterpretation
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.GdalBaseRaster
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.gdalConst
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.handleError
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.Window
import dev.tronto.kitiler.image.outgoing.adaptor.multik.DoubleImageData
import dev.tronto.kitiler.image.outgoing.adaptor.multik.FloatImageData
import dev.tronto.kitiler.image.outgoing.adaptor.multik.IntImageData
import dev.tronto.kitiler.image.outgoing.adaptor.multik.LongImageData
import dev.tronto.kitiler.image.outgoing.adaptor.multik.NDArrayImageData
import dev.tronto.kitiler.image.outgoing.adaptor.multik.normalize
import dev.tronto.kitiler.image.outgoing.port.ReadableRaster
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.ones
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.D3
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.initMemoryView
import org.jetbrains.kotlinx.multik.ndarray.operations.and
import org.jetbrains.kotlinx.multik.ndarray.operations.map
import org.jetbrains.kotlinx.multik.ndarray.operations.toDoubleArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toFloatArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toIntArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toLongArray
import kotlin.reflect.KClass

open class GdalReadableRaster(private val gdalRaster: GdalBaseRaster) :
    ReadableRaster,
    GdalBaseRaster by gdalRaster {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }
    }

    private fun DataType.toKotlinKClass(): KClass<out Number> = when (this) {
        DataType.Int8,
        DataType.UInt8,
        DataType.UInt16,
        DataType.Int16,
        DataType.Int32,
        DataType.CInt16,
        DataType.CInt32,
        -> Int::class

        DataType.UInt32, DataType.Int64 -> Long::class
        DataType.Float32, DataType.CFloat32 -> Float::class
        DataType.Float64, DataType.CFloat64 -> Double::class

        else -> throw UnsupportedOperationException("$dataType is not supported.")
    }

    private fun <T : Number> mask(data: D3Array<T>, noData: T?): D2Array<Int> = logger.logTrace("read raster mask") {
        val shape = data.shape
        if (noData == null) {
            mk.ones<Int>(shape[1], shape[2])
        } else {
            val dataBand = shape[0]
            val dataWidth = shape[2]
            val dataHeight = shape[1]
            val maskArray = IntArray(dataWidth * dataHeight) { 1 }
            fun getIndex(band: Int, height: Int, width: Int) =
                (band * dataWidth * dataHeight) + (height * dataWidth) + width

            when (data.dtype) {
                org.jetbrains.kotlinx.multik.ndarray.data.DataType.IntDataType -> {
                    val array = (data as D3Array<Int>).toIntArray()
                    val noData = noData.toInt()
                    for (h in 0..<dataHeight) {
                        index@ for (w in 0..<dataWidth) {
                            for (b in 0..<dataBand) {
                                val value = array[getIndex(b, h, w)]
                                if (value != noData) {
                                    continue@index
                                }
                            }
                            maskArray[getIndex(0, h, w)] = 0
                        }
                    }
                }

                org.jetbrains.kotlinx.multik.ndarray.data.DataType.LongDataType -> {
                    val array = (data as D3Array<Long>).toLongArray()
                    val noData = noData.toLong()
                    for (h in 0..<dataHeight) {
                        index@ for (w in 0..<dataWidth) {
                            for (b in 0..<dataBand) {
                                val value = array[getIndex(b, h, w)]
                                if (value != noData) {
                                    continue@index
                                }
                            }
                            maskArray[getIndex(0, h, w)] = 0
                        }
                    }
                }

                org.jetbrains.kotlinx.multik.ndarray.data.DataType.FloatDataType -> {
                    val array = (data as D3Array<Float>).toFloatArray()
                    val noData = noData.toFloat()
                    for (h in 0..<dataHeight) {
                        index@ for (w in 0..<dataWidth) {
                            for (b in 0..<dataBand) {
                                val value = array[getIndex(b, h, w)]
                                if (value != noData) {
                                    continue@index
                                }
                            }
                            maskArray[getIndex(0, h, w)] = 0
                        }
                    }
                }

                org.jetbrains.kotlinx.multik.ndarray.data.DataType.DoubleDataType -> {
                    val array = (data as D3Array<Double>).toDoubleArray()
                    val noData = noData.toDouble()
                    for (h in 0..<dataHeight) {
                        index@ for (w in 0..<dataWidth) {
                            for (b in 0..<dataBand) {
                                val value = array[getIndex(b, h, w)]
                                if (value != noData) {
                                    continue@index
                                }
                            }
                            maskArray[getIndex(0, h, w)] = 0
                        }
                    }
                }

                else -> throw UnsupportedOperationException("$dataType is not supported.")
            }
            mk.ndarray(maskArray, dataHeight, dataWidth)
        }
    }

    private fun <T : Number> pad(
        data: D3Array<T>,
        mask: D2Array<Int>,
        noDataValue: T?,
        left: Int,
        right: Int,
        upper: Int,
        lower: Int,
        kClass: KClass<T>,
    ): Pair<D3Array<T>, D2Array<Int>> {
        var data = data
        var mask = mask
        val mkDataType = org.jetbrains.kotlinx.multik.ndarray.data.DataType.ofKClass(kClass)

        fun createPad(vararg shape: Int): NDArray<T, D3> {
            val pad = if (noDataValue == null || noDataValue.toInt() == 0) {
                mk.zeros<T, D3>(shape, mkDataType)
            } else {
                val view = initMemoryView<T>(shape.reduce(Int::times), mkDataType) { noDataValue }
                D3Array<T>(view, shape = shape, dim = D3)
            }
            return pad
        }

        if (left > 0 && right > 0) {
            val leftPad = createPad(data.shape[0], data.shape[1], left)
            val rightPad = createPad(data.shape[0], data.shape[1], right)
            data = leftPad.cat(listOf(data, rightPad))

            val leftMaskPad = mk.zeros<Int>(data.shape[1], left)
            val rightMaskPad = mk.zeros<Int>(data.shape[1], right)
            mask = leftMaskPad.cat(listOf(mask, rightMaskPad), 1)
        } else if (left > 0) {
            val leftPad = createPad(data.shape[0], data.shape[1], left)
            data = leftPad.cat(data, 2)
            mask = mk.zeros<Int>(data.shape[1], left).cat(mask, 1)
        } else if (right > 0) {
            val rightPad = createPad(data.shape[0], data.shape[1], right)
            data = data.cat(rightPad, 2)
            mask = mask.cat(mk.zeros<Int>(data.shape[1], right), 1)
        }

        if (upper > 0 && lower > 0) {
            val upperPad = createPad(data.shape[0], upper, data.shape[2])
            val lowerPad = createPad(data.shape[0], lower, data.shape[2])
            data = upperPad.cat(listOf(data, lowerPad), 1)

            val upperMaskPad = mk.zeros<Int>(upper, data.shape[2])
            val lowerMaskPad = mk.zeros<Int>(lower, data.shape[2])
            mask = upperMaskPad.cat(listOf(mask, lowerMaskPad), 0)
        } else if (upper > 0) {
            val upperPad = createPad(data.shape[0], upper, data.shape[2])
            data = upperPad.cat(data, 1)
            mask = mk.zeros<Int>(upper, data.shape[2]).cat(mask, 0)
        } else if (lower > 0) {
            val lowerPad = createPad(data.shape[0], lower, data.shape[2])
            data = data.cat(lowerPad, 1)
            mask = mask.cat(mk.zeros<Int>(lower, data.shape[2]), 0)
        }

        return data.normalize() to mask.normalize()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Number> getReader(kClass: KClass<T>) = when (kClass) {
        Int::class -> IntDataReader()
        Long::class -> LongDataReader()
        Float::class -> FloatDataReader()
        Double::class -> DoubleDataReader()
        else -> throw UnsupportedOperationException("$kClass is not supported.")
    } as Reader<T>

    private fun <T> read(
        window: Window,
        width: Int,
        height: Int,
        bandIndexes: List<BandIndex>?,
        nodata: T?,
        leftPad: Int,
        rightPad: Int,
        upperPad: Int,
        lowerPad: Int,
        kClass: KClass<T>,
    ): NDArrayImageData<T> where T : Number, T : Comparable<T> = logger.logTrace("read") {
        val alphaBand = (1..bandCount).reversed().find {
            bandInfo(BandIndex(it)).colorInterpolation == ColorInterpretation.AlphaBand
        }
        val bandList = bandIndexes?.map { it.value }?.toIntArray() ?: if (alphaBand == null) {
            IntArray(bandCount) { it + 1 }
        } else {
            (1..bandCount).filter { it != alphaBand }.toIntArray()
        }

        @Suppress("UNCHECKED_CAST")
        val noDataValue = when (kClass) {
            Int::class -> (nodata ?: this.noDataValue)?.toInt()
            Long::class -> (nodata ?: this.noDataValue)?.toLong()
            Float::class -> (nodata ?: this.noDataValue)?.toFloat()
            Double::class -> (nodata ?: this.noDataValue)?.toDouble()
            else -> throw UnsupportedOperationException()
        } as T?

        val reader = getReader(kClass)

        val (data, mask) = if (alphaBand != null) {
            val alphaBandInfo = bandInfo(BandIndex(alphaBand))

            val (data, mask) = if (alphaBandInfo.dataType != dataType) {
                val data = reader.readData(bandList, width, height, window).normalize()
                val maskReader = getReader(alphaBandInfo.dataType.toKotlinKClass())
                val mask2 = maskReader.readData(intArrayOf(alphaBand), width, height, window)
                val mask = mask2.map { if (it.toInt() == 0) 0 else 1 }.squeeze().asD2Array()
                data to mask
            } else {
                val dataAndMask = reader.readData(bandList + alphaBand, width, height, window)
                val data = dataAndMask[bandList.indices] as D3Array<T>
                val mask2 = dataAndMask[bandList.size] as D2Array<T>
                val mask = mask2.map { if (it.toInt() == 0) 0 else 1 }.squeeze().asD2Array()
                data to mask
            }
            val resultMask = if (noDataValue != null) {
                (mask and mask(data, noDataValue))
            } else {
                mask
            }
            data to resultMask
        } else {
            val data = reader.readData(bandList, width, height, window)
            val mask = mask(data, noDataValue)
            data to mask
        }
        val (resultData, resultMask) = pad(
            data.normalize(),
            mask.normalize(),
            noDataValue,
            leftPad,
            rightPad,
            upperPad,
            lowerPad,
            kClass
        )

        @Suppress("UNCHECKED_CAST")
        val imageData = when (kClass) {
            Int::class -> IntImageData(resultData as D3Array<Int>, resultMask, dataType)
            Long::class -> LongImageData(resultData as D3Array<Long>, resultMask, dataType)
            Float::class -> FloatImageData(resultData as D3Array<Float>, resultMask, dataType)
            Double::class -> DoubleImageData(resultData as D3Array<Double>, resultMask, dataType)
            else -> throw UnsupportedOperationException()
        }

        imageData as NDArrayImageData<T>
    }

    override fun read(
        window: Window,
        width: Int,
        height: Int,
        bandIndexes: List<BandIndex>?,
        nodata: Number?,
    ): NDArrayImageData<*> {
        val leftOver = if (window.xOffset < 0) -window.xOffset else 0
        val rightOver = if (window.xOffset + window.width > this.width) {
            window.xOffset + window.width - this.width
        } else {
            0
        }

        val upperOver = if (window.yOffset < 0) -window.yOffset else 0
        val lowerOver = if (window.yOffset + window.height > this.height) {
            window.yOffset + window.height - this.height
        } else {
            0
        }

        val widthRatio = width.toDouble() / window.width
        val heightRatio = height.toDouble() / window.height

        val leftPad = if (leftOver > 0) (leftOver * widthRatio).toInt() else 0
        val rightPad = if (rightOver > 0) (rightOver * widthRatio).toInt() else 0
        val upperPad = if (upperOver > 0) (upperOver * heightRatio).toInt() else 0
        val lowerPad = if (lowerOver > 0) (lowerOver * heightRatio).toInt() else 0

        val newWindow = Window(
            window.xOffset + leftOver,
            window.yOffset + upperOver,
            window.width - leftOver - rightOver,
            window.height - upperOver - lowerOver
        )
        val newWidth = width - leftPad - rightPad
        val newHeight = height - upperPad - lowerPad

        val imageData = when (dataType.toKotlinKClass()) {
            Int::class -> read<Int>(
                newWindow,
                newWidth,
                newHeight,
                bandIndexes,
                nodata?.toInt(),
                leftPad, rightPad, upperPad, lowerPad,
                Int::class
            )

            Long::class -> read(
                newWindow,
                newWidth,
                newHeight,
                bandIndexes,
                nodata?.toLong(),
                leftPad, rightPad, upperPad, lowerPad,
                Long::class
            )

            Float::class -> read(
                newWindow,
                newWidth,
                newHeight,
                bandIndexes,
                nodata?.toFloat(),
                leftPad, rightPad, upperPad, lowerPad,
                Float::class
            )

            Double::class -> read(
                newWindow,
                newWidth,
                newHeight,
                bandIndexes,
                nodata?.toDouble(),
                leftPad, rightPad, upperPad, lowerPad,
                Double::class
            )

            else -> throw UnsupportedOperationException("$dataType is not supported.")
        }

        return imageData
    }

    override fun close() {
        dataset.delete()
    }

    private abstract inner class Reader<T : Number> {
        fun readData(bandList: IntArray, width: Int, height: Int, window: Window): D3Array<T> =
            logger.logTrace("read gdal") {
                read(bandList, width, height, window)
            }

        abstract fun read(bandList: IntArray, width: Int, height: Int, window: Window): D3Array<T>
    }

    private inner class IntDataReader : Reader<Int>() {

        private fun readIntData(bandList: IntArray, width: Int, height: Int, window: Window): D3Array<Int> {
            val arr = IntArray(bandList.size * width * height)
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
            return mk.ndarray(arr, bandList.size, height, width)
        }

        override fun read(bandList: IntArray, width: Int, height: Int, window: Window): D3Array<Int> =
            readIntData(bandList, width, height, window)
    }

    private inner class LongDataReader : Reader<Long>() {

        private fun readLongData(bandList: IntArray, width: Int, height: Int, window: Window): D3Array<Long> {
            val arr = LongArray(bandList.size * width * height)
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
            return mk.ndarray(arr, bandList.size, height, width)
        }

        override fun read(bandList: IntArray, width: Int, height: Int, window: Window): D3Array<Long> =
            readLongData(bandList, width, height, window)
    }

    private inner class FloatDataReader : Reader<Float>() {

        private fun readFloatData(bandList: IntArray, width: Int, height: Int, window: Window): D3Array<Float> {
            val arr = FloatArray(bandList.size * width * height)
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
            return mk.ndarray(arr, bandList.size, height, width)
        }

        override fun read(bandList: IntArray, width: Int, height: Int, window: Window): D3Array<Float> =
            readFloatData(bandList, width, height, window)
    }

    private inner class DoubleDataReader : Reader<Double>() {

        private fun readDoubleData(bandList: IntArray, width: Int, height: Int, window: Window): D3Array<Double> {
            val arr = DoubleArray(bandList.size * width * height)
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
            return mk.ndarray(arr, bandList.size, height, width)
        }

        override fun read(bandList: IntArray, width: Int, height: Int, window: Window): D3Array<Double> =
            readDoubleData(bandList, width, height, window)
    }
}
