package dev.tronto.kitiler.image.outgoing.adaptor.multik

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.image.domain.ImageData
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array

class FloatImageData(
    data: D3Array<Float>,
    mask: D2Array<Int>,
    override val dataType: DataType,
    vararg options: OptionProvider<*>,
) : NDArrayImageData<Float>(data, mask, *options),
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
        mask: D2Array<Int>,
        vararg options: OptionProvider<*>,
    ): NDArrayImageData<Float> = FloatImageData(data, mask, dataType, *options)
}
