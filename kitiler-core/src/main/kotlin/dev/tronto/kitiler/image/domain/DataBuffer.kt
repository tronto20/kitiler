package dev.tronto.kitiler.image.domain

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.utils.ArrayManager
import java.nio.ByteBuffer

interface DataBuffer {
    val byteBuffer: ByteBuffer
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
        get() {
            require(isIntArray) {
            }
            byteBuffer.rewind()
            val array = ArrayManager.getIntArray(byteBuffer.limit())
            byteBuffer.asIntBuffer().get(array)
            byteBuffer.rewind()
            return array
        }
    val longArray: LongArray
        get() {
            require(isLongArray) {
            }
            byteBuffer.rewind()
            val array = ArrayManager.getLongArray(byteBuffer.limit())
            byteBuffer.asLongBuffer().get(array)
            byteBuffer.rewind()
            return array
        }
    val floatArray: FloatArray
        get() {
            require(isFloatArray) {
            }
            byteBuffer.rewind()
            val array = ArrayManager.getFloatArray(byteBuffer.limit())
            byteBuffer.asFloatBuffer().get(array)
            byteBuffer.rewind()
            return array
        }
    val doubleArray: DoubleArray
        get() {
            require(isDoubleArray) {
            }
            byteBuffer.rewind()
            val array = ArrayManager.getDoubleArray(byteBuffer.limit())
            byteBuffer.asDoubleBuffer().get(array)
            byteBuffer.rewind()
            return array
        }
}
