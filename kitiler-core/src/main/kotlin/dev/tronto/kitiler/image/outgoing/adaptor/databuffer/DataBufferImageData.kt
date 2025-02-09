package dev.tronto.kitiler.image.outgoing.adaptor.databuffer

import dev.tronto.kitiler.core.domain.BandInfo
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.domain.OptionContext
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.DataBuffer
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.IntArrayDataBuffer
import dev.tronto.kitiler.image.outgoing.adaptor.multik.linearRescaleToInt
import io.github.oshai.kotlinlogging.KotlinLogging
import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Location

class DataBufferImageData(
    override val dataType: DataType,
    override val bandCount: Int,
    override val width: Int,
    override val height: Int,
    override val bandInfo: List<BandInfo>,
    private val data: DataBuffer,
    private val validArray: BooleanArray?,
    vararg options: OptionProvider<*>,
) : OptionContext by OptionContext.Companion.wrap(*options),
    ImageData {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val masked: Boolean = validArray?.any { !it } == true

    override suspend fun <T, R> rescale(
        rangeFrom: List<ClosedRange<T>>,
        rangeTo: List<ClosedRange<R>>,
        dataType: DataType,
    ): ImageData where T : Number, T : Comparable<T>, R : Number, R : Comparable<R> {
        logger.logTrace("Rescaling ImageData") {
            val dataBuffer = when (dataType) {
                DataType.Int8,
                DataType.UInt8,
                DataType.UInt16,
                DataType.Int16,
                DataType.Int32,
                DataType.CInt16,
                DataType.CInt32,
                -> rescaleToInt(rangeFrom, rangeTo.map { it.start.toInt()..it.endInclusive.toInt() })

                DataType.UInt32,
                DataType.Int64,
                DataType.Float32,
                DataType.CFloat32,
                DataType.Float64,
                DataType.CFloat64,
                DataType.UInt64,
                -> throw UnsupportedOperationException()
            }
            return copy(dataType = dataType, data = dataBuffer)
        }
    }

    private suspend fun <T> rescaleToInt(
        rangeFrom: List<ClosedRange<T>>,
        rangeTo: List<IntRange>,
    ): DataBuffer where T : Number, T : Comparable<T> = when {
        data.isIntArray
        -> {
            val dataArray = data.intArray
            val pixelSizePerBand = height * width

            (0..<bandCount).forEach { band ->
                val from =
                    rangeFrom.getOrElse(band) { rangeFrom[0] }.let { it.start.toInt()..it.endInclusive.toInt() }
                val to = rangeTo.getOrElse(band) { rangeTo[0] }
                linearRescaleToInt(dataArray, pixelSizePerBand * band, pixelSizePerBand, from, to)
            }
            IntArrayDataBuffer(dataArray)
        }

        data.isLongArray -> {
            val dataArray = data.longArray
            val targetArray = IntArray(dataArray.size)
            val pixelSizePerBand = height * width

            (0..<bandCount).forEach { band ->
                val from =
                    rangeFrom.getOrElse(band) { rangeFrom[0] }.let { it.start.toLong()..it.endInclusive.toLong() }
                val to = rangeTo.getOrElse(band) { rangeTo[0] }
                linearRescaleToInt(dataArray, pixelSizePerBand * band, pixelSizePerBand, from, to, targetArray)
            }
            IntArrayDataBuffer(targetArray)
        }

        data.isFloatArray -> {
            val dataArray = data.floatArray
            val targetArray = IntArray(dataArray.size)
            val pixelSizePerBand = height * width

            (0..<bandCount).forEach { band ->
                val from =
                    rangeFrom.getOrElse(band) { rangeFrom[0] }.let { it.start.toFloat()..it.endInclusive.toFloat() }
                val to = rangeTo.getOrElse(band) { rangeTo[0] }
                linearRescaleToInt(dataArray, pixelSizePerBand * band, pixelSizePerBand, from, to, targetArray)
            }
            IntArrayDataBuffer(targetArray)
        }

        data.isDoubleArray -> {
            val dataArray = data.doubleArray
            val targetArray = IntArray(dataArray.size)
            val pixelSizePerBand = height * width

            (0..<bandCount).forEach { band ->
                val from =
                    rangeFrom.getOrElse(band) { rangeFrom[0] }
                        .let { it.start.toDouble()..it.endInclusive.toDouble() }
                val to = rangeTo.getOrElse(band) { rangeTo[0] }
                linearRescaleToInt(dataArray, pixelSizePerBand * band, pixelSizePerBand, from, to, targetArray)
            }
            IntArrayDataBuffer(targetArray)
        }

        else -> throw UnsupportedOperationException()
    }

    override fun mask(geom: Geometry): ImageData {
        logger.logTrace("Masking ImageData") {
            val index = IndexedPointInAreaLocator(geom)
            val validArray = BooleanArray(height * width)
            this.validArray?.copyInto(validArray)
            for (i in validArray.indices) {
                val coord = Coordinate((i % width).toDouble(), (i / width).toDouble())
                if (validArray[i] && index.locate(coord) == Location.EXTERIOR) {
                    validArray[i] = false
                }
            }
            return copy(validArray = validArray)
        }
    }

    fun copy(
        dataType: DataType = this.dataType,
        bandCount: Int = this.bandCount,
        width: Int = this.width,
        height: Int = this.height,
        bandInfo: List<BandInfo> = this.bandInfo,
        data: DataBuffer = this.data,
        validArray: BooleanArray? = this.validArray,
        vararg options: OptionProvider<*> = this.getAllOptionProviders().toTypedArray(),
    ): ImageData = DataBufferImageData(
        dataType,
        bandCount,
        width,
        height,
        bandInfo,
        data,
        validArray,
        options = options
    )

    override fun getBandBuffer(): DataBuffer = data

    override fun getValidArray(): BooleanArray? = validArray
}
