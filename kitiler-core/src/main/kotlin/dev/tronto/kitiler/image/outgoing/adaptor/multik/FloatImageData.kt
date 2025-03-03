package dev.tronto.kitiler.image.outgoing.adaptor.multik

import dev.tronto.kitiler.core.domain.BandInfo
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.image.domain.DataBuffer
import dev.tronto.kitiler.image.domain.FloatArrayDataBuffer
import dev.tronto.kitiler.image.domain.ImageData
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.operations.toFloatArray

class FloatImageData(
    data: D3Array<Float>,
    valid: BooleanArray?,
    override val dataType: DataType,
    override val bandInfo: List<BandInfo>,
    vararg options: OptionProvider<*>,
) : NDArrayImageData<Float>(data, valid, *options),
    ImageData {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }

        @JvmStatic
        private val availableTypes = listOf(
            DataType.Float32,
            DataType.CFloat32
        )
    }

    init {
        require(dataType in availableTypes) {
            "dataType must be in $availableTypes"
        }
    }

    override fun Number.asType(): Float = toFloat()

    override fun copy(
        data: D3Array<Float>,
        valid: BooleanArray?,
        vararg options: OptionProvider<*>,
    ): NDArrayImageData<Float> = FloatImageData(data, valid, dataType, bandInfo, *options)

    override fun getBandBuffer(): DataBuffer {
        val arr = data.toFloatArray()
        return FloatArrayDataBuffer(arr)
    }
}
