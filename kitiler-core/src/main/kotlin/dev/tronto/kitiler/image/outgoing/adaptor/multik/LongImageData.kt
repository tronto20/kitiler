package dev.tronto.kitiler.image.outgoing.adaptor.multik

import dev.tronto.kitiler.core.domain.BandInfo
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.image.domain.ImageData
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.operations.toLongArray
import java.nio.Buffer
import java.nio.LongBuffer

class LongImageData(
    data: D3Array<Long>,
    mask: D2Array<Int>,
    override val dataType: DataType,
    override val bandInfo: List<BandInfo>,
    vararg options: OptionProvider<*>,
) : NDArrayImageData<Long>(data, mask, *options),
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
        mask: D2Array<Int>,
        vararg options: OptionProvider<*>,
    ): NDArrayImageData<Long> = LongImageData(data, mask, dataType, bandInfo, *options)

    override fun getBandBuffers(): List<Buffer> {
        val arr = data.toLongArray()
        val size = width * height
        return (0..<bandCount).map {
            LongBuffer.wrap(arr, it * size, size)
        }
    }
}
