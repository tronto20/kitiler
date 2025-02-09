package dev.tronto.kitiler.image.outgoing.adaptor.multik

import dev.tronto.kitiler.core.domain.BandInfo
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.image.domain.DataBuffer
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.IntArrayDataBuffer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.operations.toIntArray

class IntImageData(
    data: D3Array<Int>,
    valid: BooleanArray?,
    override val dataType: DataType,
    override val bandInfo: List<BandInfo>,
    vararg options: OptionProvider<*>,
) : NDArrayImageData<Int>(data, valid, *options),
    ImageData {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }

        @JvmStatic
        private val availableTypes = listOf(
            DataType.Int8,
            DataType.UInt8,
            DataType.UInt16,
            DataType.Int16,
            DataType.Int32,
            DataType.CInt16,
            DataType.CInt32
        )
    }

    init {
        require(dataType in availableTypes) {
            "dataType must be in $availableTypes"
        }
    }

    override fun Number.asType(): Int = toInt()

    override fun copy(
        data: D3Array<Int>,
        valid: BooleanArray?,
        vararg options: OptionProvider<*>,
    ): NDArrayImageData<Int> = IntImageData(data, valid, dataType, bandInfo, *options)

    override fun getBandBuffer(): DataBuffer {
        val arr = data.toIntArray()
        return IntArrayDataBuffer(arr)
    }
}
