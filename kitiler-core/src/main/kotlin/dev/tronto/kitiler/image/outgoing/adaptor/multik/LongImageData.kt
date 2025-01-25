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
import org.jetbrains.kotlinx.multik.ndarray.operations.toLongArray

class LongImageData(
    data: D3Array<Long>,
    valid: BooleanArray?,
    override val dataType: DataType,
    override val bandInfo: List<BandInfo>,
    vararg options: OptionProvider<*>,
) : NDArrayImageData<Long>(data, valid, *options),
    ImageData {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }

        @JvmStatic
        private val availableTypes = listOf(
            DataType.UInt32,
            DataType.Int64
        )
    }

    init {
        require(dataType in availableTypes) {
            "dataType must be in $availableTypes"
        }
    }

    override fun Number.asType(): Long = toLong()

    override fun copy(
        data: D3Array<Long>,
        valid: BooleanArray?,
        vararg options: OptionProvider<*>,
    ): NDArrayImageData<Long> = LongImageData(data, valid, dataType, bandInfo, *options)

    override fun getBandBuffer(): DataBuffer {
        val arr = data.toLongArray()
        val buffer = ByteBufferManager.get(arr.size * Long.SIZE_BYTES)
        buffer.asLongBuffer().put(arr)
        return SimpleDataBuffer(buffer.rewind(), DataType.Int64)
    }
}
