package dev.tronto.kitiler.image.domain

import dev.tronto.kitiler.core.domain.DataType

class IntArrayDataBuffer(override val intArray: IntArray) : DataBuffer {
    override val dataType: DataType
        get() = DataType.Int32
}

class LongArrayDataBuffer(override val longArray: LongArray) : DataBuffer {
    override val dataType: DataType
        get() = DataType.Int64
}

class FloatArrayDataBuffer(override val floatArray: FloatArray) : DataBuffer {
    override val dataType: DataType
        get() = DataType.Float32
}

class DoubleArrayDataBuffer(override val doubleArray: DoubleArray) : DataBuffer {
    override val dataType: DataType
        get() = DataType.Float64
}
