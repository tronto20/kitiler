package dev.tronto.kitiler.image.outgoing.adaptor.multik

import dev.tronto.kitiler.core.domain.BandInfo
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.image.domain.ImageData
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.operations.toIntArray
import java.nio.Buffer
import java.nio.IntBuffer

class IntImageData(
    data: D3Array<Int>,
    mask: D2Array<Int>,
    override val dataType: DataType,
    override val bandInfo: List<BandInfo>,
    vararg options: OptionProvider<*>,
) : NDArrayImageData<Int>(data, mask, *options),
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
        mask: D2Array<Int>,
        vararg options: OptionProvider<*>,
    ): NDArrayImageData<Int> = IntImageData(data, mask, dataType, bandInfo, *options)

    override fun getBandBuffers(): List<Buffer> {
        val arr = data.toIntArray()
        val size = width * height
        return (0..<bandCount).map {
            IntBuffer.wrap(arr, it * size, size)
        }
    }
}
