package dev.tronto.kitiler.image.outgoing.adaptor.multik

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.domain.OptionContext
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.ImageData
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.operations.toDoubleArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toFloatArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toIntArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toLongArray
import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Location

/**
 *  mask 는 0 이 유효하지 않은 값, 1이 유효한 값.
 */
sealed class NDArrayImageData<T>(
    internal val data: D3Array<T>,
    internal val valid: BooleanArray?,
    vararg options: OptionProvider<*>,
) : OptionContext by OptionContext.wrap(*options),
    ImageData where T : Number, T : Comparable<T> {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }
    }

    final override val bandCount
        get() = data.shape[0]

    final override val width
        get() = data.shape[2]

    final override val height
        get() = data.shape[1]

    final override val masked: Boolean by lazy {
        valid?.any { !it } == true
    }

    init {
        require(valid == null || (height * width) == valid.size) {
            "data and mask shape must be equal. (data: ${data.shape.toList()}, mask: ${valid?.size})"
        }
    }

    abstract fun copy(
        data: D3Array<T> = this.data,
        valid: BooleanArray? = this.valid,
        vararg options: OptionProvider<*> = this.getAllOptionProviders().toTypedArray(),
    ): NDArrayImageData<T>

    override fun mask(geom: Geometry): ImageData {
        val index = IndexedPointInAreaLocator(geom)
        val validArray = BooleanArray(height * width)
        valid?.copyInto(validArray)
        for (i in validArray.indices) {
            val coord = Coordinate((i % width).toDouble(), (i / width).toDouble())
            if (validArray[i] && index.locate(coord) == Location.EXTERIOR) {
                validArray[i] = false
            }
        }
        return copy(valid = validArray)
    }

    abstract fun Number.asType(): T

    override suspend fun <I, R> rescale(
        rangeFrom: List<ClosedRange<I>>,
        rangeTo: List<ClosedRange<R>>,
        dataType: DataType,
    ): ImageData where R : Number, I : Number, I : Comparable<I>, R : Comparable<R> {
        val rangeFrom = rangeFrom.map {
            NumberRange(it.start.asType()..it.endInclusive.asType())
        }
        return logger.logTrace("rescale") {
            when (dataType) {
                DataType.Int8,
                DataType.UInt8,
                DataType.UInt16,
                DataType.Int16,
                DataType.Int32,
                DataType.CInt16,
                DataType.CInt32,
                -> {
                    val rangeTo = rangeTo.map {
                        it.start.toInt()..it.endInclusive.toInt()
                    }

                    val rescaled = rescaleToInt(rangeFrom, rangeTo)
                    IntImageData(rescaled, valid, dataType, bandInfo, *getAllOptionProviders().toTypedArray())
                }

                DataType.UInt32,
                DataType.Int64,
                DataType.Float32,
                DataType.CFloat32,
                DataType.Float64,
                DataType.CFloat64,
                DataType.UInt64,
                -> throw UnsupportedOperationException()
            }
        }
    }

    private fun rescaleToInt(rangeFrom: List<ClosedRange<T>>, rangeTo: List<IntRange>): D3Array<Int> =
        when (this.dataType) {
            DataType.Int8,
            DataType.UInt8,
            DataType.UInt16,
            DataType.Int16,
            DataType.Int32,
            -> {
                val dataArray = (data as D3Array<Int>).toIntArray()
                val pixelSizePerBand = data.shape[1] * data.shape[2]

                (0..<data.shape[0]).forEach { band ->
                    val from =
                        rangeFrom.getOrElse(band) { rangeFrom[0] }.let { it.start.toInt()..it.endInclusive.toInt() }
                    val to = rangeTo.getOrElse(band) { rangeTo[0] }
                    linearRescaleToInt(dataArray, pixelSizePerBand * band, pixelSizePerBand, from, to)
                }
                mk.ndarray(dataArray, data.shape[0], data.shape[1], data.shape[2])
            }

            DataType.UInt32,
            DataType.Int64,
            -> {
                val dataArray = (data as D3Array<Long>).toLongArray()
                val targetArray = IntArray(dataArray.size)
                val pixelSizePerBand = data.shape[1] * data.shape[2]
                (0..<data.shape[0]).forEach { band ->
                    val from =
                        rangeFrom.getOrElse(band) { rangeFrom[0] }
                            .let { it.start.toLong()..it.endInclusive.toLong() }
                    val to = rangeTo.getOrElse(band) { rangeTo[0] }
                    linearRescaleToInt(dataArray, pixelSizePerBand * band, pixelSizePerBand, from, to, targetArray)
                }
                mk.ndarray(targetArray, data.shape[0], data.shape[1], data.shape[2])
            }

            DataType.Float32,
            DataType.CFloat32,
            -> {
                val dataArray = (data as D3Array<Float>).toFloatArray()
                val targetArray = IntArray(dataArray.size)
                val pixelSizePerBand = data.shape[1] * data.shape[2]
                (0..<data.shape[0]).forEach { band ->
                    val from =
                        rangeFrom.getOrElse(band) { rangeFrom[0] }
                            .let { it.start.toFloat()..it.endInclusive.toFloat() }
                    val to = rangeTo.getOrElse(band) { rangeTo[0] }
                    linearRescaleToInt(dataArray, pixelSizePerBand * band, pixelSizePerBand, from, to, targetArray)
                }
                mk.ndarray(targetArray, data.shape[0], data.shape[1], data.shape[2])
            }

            DataType.Float64,
            DataType.CFloat64,
            -> {
                val dataArray = (data as D3Array<Double>).toDoubleArray()
                val targetArray = IntArray(dataArray.size)
                val pixelSizePerBand = data.shape[1] * data.shape[2]
                (0..<data.shape[0]).forEach { band ->
                    val from =
                        rangeFrom.getOrElse(band) { rangeFrom[0] }
                            .let { it.start.toDouble()..it.endInclusive.toDouble() }
                    val to = rangeTo.getOrElse(band) { rangeTo[0] }
                    linearRescaleToInt(dataArray, pixelSizePerBand * band, pixelSizePerBand, from, to, targetArray)
                }
                mk.ndarray(targetArray, data.shape[0], data.shape[1], data.shape[2])
            }

            else -> throw UnsupportedOperationException("${this.dataType} is not supported.")
        }

    override fun getValidArray(): BooleanArray? = valid
}
