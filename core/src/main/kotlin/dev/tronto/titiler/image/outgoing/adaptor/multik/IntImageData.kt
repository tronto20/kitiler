package dev.tronto.titiler.image.outgoing.adaptor.multik

import dev.tronto.titiler.core.domain.DataType
import dev.tronto.titiler.image.outgoing.port.ImageData
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array

internal class IntImageData(
    data: D3Array<Int>,
    mask: D2Array<Byte>,
    override val dataType: DataType,
) : ImageData, NDArrayImageData<Int>(data, mask) {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }
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

    override fun Number.asType(): Int {
        return toInt()
    }

    override fun copy(data: D3Array<Int>, mask: D2Array<Byte>): NDArrayImageData<Int> {
        return IntImageData(data, mask, dataType)
    }
}