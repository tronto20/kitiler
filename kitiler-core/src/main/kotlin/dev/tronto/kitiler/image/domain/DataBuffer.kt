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
            val buffer = byteBuffer.asIntBuffer()
            val array = ArrayManager.getIntArray(buffer.limit())
            buffer.get(array)
            byteBuffer.rewind()
            return array
        }
    val longArray: LongArray
        get() {
            require(isLongArray) {
            }
            byteBuffer.rewind()
            val buffer = byteBuffer.asLongBuffer()
            val array = ArrayManager.getLongArray(buffer.limit())
            buffer.get(array)
            byteBuffer.rewind()
            return array
        }
    val floatArray: FloatArray
        get() {
            require(isFloatArray) {
            }
            byteBuffer.rewind()
            val buffer = byteBuffer.asFloatBuffer()
            val array = ArrayManager.getFloatArray(buffer.limit())
            buffer.get(array)
            byteBuffer.rewind()
            return array
        }
    val doubleArray: DoubleArray
        get() {
            require(isDoubleArray) {
            }
            byteBuffer.rewind()
            val buffer = byteBuffer.asDoubleBuffer()
            val array = ArrayManager.getDoubleArray(buffer.limit())
            buffer.get(array)
            byteBuffer.rewind()
            return array
        }
}
