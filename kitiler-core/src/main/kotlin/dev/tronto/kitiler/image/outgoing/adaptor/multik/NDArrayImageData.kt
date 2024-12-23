package dev.tronto.kitiler.image.outgoing.adaptor.multik

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.domain.OptionContext
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.ImageData
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.data.view
import org.jetbrains.kotlinx.multik.ndarray.operations.all
import org.jetbrains.kotlinx.multik.ndarray.operations.mapMultiIndexed
import org.jetbrains.kotlinx.multik.ndarray.operations.stack
import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Location

/**
 *  mask 는 0 이 유효하지 않은 값, 1이 유효한 값.
 */
sealed class NDArrayImageData<T>(
    internal val data: D3Array<T>,
    internal val mask: D2Array<Int>,
    vararg options: OptionProvider<*>,
) : OptionContext by OptionContext.wrap(*options),
    ImageData where T : Number, T : Comparable<T> {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }
    }

    final override val band
        get() = data.shape[0]

    final override val width
        get() = data.shape[2]

    final override val height
        get() = data.shape[1]

    final override val masked: Boolean by lazy { mask.all { true } }

    init {
        val maskShape = mask.shape
        require(width == maskShape[1] && height == maskShape[0]) {
            "data and mask shape must be equal. (data: ${data.shape.toList()}, mask: ${mask.shape.toList()})"
        }
    }

    abstract fun copy(
        data: D3Array<T> = this.data,
        mask: D2Array<Int> = this.mask,
        vararg options: OptionProvider<*> = this.getAllOptionProviders().toTypedArray(),
    ): NDArrayImageData<T>

    override fun mask(geom: Geometry): ImageData {
        val index = IndexedPointInAreaLocator(geom)
        val mask = mask.mapMultiIndexed { (h, w), byte ->
            if (byte == 0) {
                byte
            } else if (index.locate(Coordinate(w.toDouble(), h.toDouble())) == Location.EXTERIOR) {
                0
            } else {
                1
            }
        }
        return copy(mask = mask)
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
                    IntImageData(rescaled, mask, dataType, *getAllOptionProviders().toTypedArray())
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

    private suspend fun rescaleToInt(rangeFrom: List<NumberRange<T>>, rangeTo: List<IntRange>): D3Array<Int> {
        val rescaled = (0..<data.shape[0]).map { band ->
            CoroutineScope(Dispatchers.Default).async {
                val from = rangeFrom.getOrElse(band) { rangeFrom[0] }
                val to = rangeTo.getOrElse(band) { rangeTo[0] }
                linearRescale<T, D2>(data.view(band), from, to)
            }
        }
        return mk.stack(rescaled.awaitAll())
    }
}
