package dev.tronto.kitiler.image.outgoing.adaptor.multik

import dev.tronto.kitiler.core.domain.BandInfo
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.utils.ByteBufferManager
import dev.tronto.kitiler.image.domain.DataBuffer
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.SimpleDataBuffer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.operations.toDoubleArray

class DoubleImageData(
    data: D3Array<Double>,
    valid: BooleanArray?,
    override val dataType: DataType,
    override val bandInfo: List<BandInfo>,
    vararg options: OptionProvider<*>,
) : NDArrayImageData<Double>(data, valid, *options),
    ImageData {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }

        @JvmStatic
        private val availableTypes = listOf(
            DataType.Float64,
            DataType.CFloat64
        )
    }

    init {
        require(dataType in availableTypes) {
            "dataType must be in $availableTypes"
        }
    }

    override fun Number.asType(): Double = toDouble()

    override fun copy(
        data: D3Array<Double>,
        valid: BooleanArray?,
        vararg options: OptionProvider<*>,
    ): NDArrayImageData<Double> = DoubleImageData(data, valid, dataType, bandInfo, *options)

    override fun getBandBuffer(): DataBuffer {
        val arr = data.toDoubleArray()
        val buffer = ByteBufferManager.get(arr.size * Double.SIZE_BYTES)
        buffer.asDoubleBuffer().put(arr)
        return SimpleDataBuffer(buffer.rewind(), DataType.Float64)
    }
}
