package dev.tronto.kitiler.image.domain

import dev.tronto.kitiler.core.domain.DataType

interface DataBuffer {
    val dataType: DataType

    val isIntArray: Boolean
        get() = dataType == DataType.Int32
    val isFloatArray: Boolean
        get() = dataType == DataType.Float32
    val isLongArray: Boolean
        get() = dataType == DataType.Int64
    val isDoubleArray: Boolean
        get() = dataType == DataType.Float64

    val intArray: IntArray
        get() = if (isIntArray) {
            throw NotImplementedError()
        } else {
            throw UnsupportedOperationException()
        }
    val longArray: LongArray
        get() = if (isLongArray) {
            throw NotImplementedError()
        } else {
            throw UnsupportedOperationException()
        }
    val floatArray: FloatArray
        get() = if (isFloatArray) {
            throw NotImplementedError()
        } else {
            throw UnsupportedOperationException()
        }
    val doubleArray: DoubleArray
        get() = if (isDoubleArray) {
            throw NotImplementedError()
        } else {
            throw UnsupportedOperationException()
        }
}
