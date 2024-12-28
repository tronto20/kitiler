package dev.tronto.kitiler.image.outgoing.adaptor.multik

import org.jetbrains.kotlinx.multik.ndarray.data.DataType
import org.jetbrains.kotlinx.multik.ndarray.data.Dimension
import org.jetbrains.kotlinx.multik.ndarray.data.MemoryViewIntArray
import org.jetbrains.kotlinx.multik.ndarray.data.MultiArray
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toDoubleArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toFloatArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toIntArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toLongArray

fun linearRescaleToInt(
    array: IntArray,
    offset: Int,
    size: Int,
    from: IntRange,
    to: IntRange,
    targetArray: IntArray = array,
): IntArray {
    val ratio = (to.last - to.first).toDouble() / (from.last - from.first)
    for (index in offset until offset + size) {
        val value = array[index]
        targetArray[index] = if (value <= from.first) {
            to.first
        } else if (value >= from.last) {
            to.last
        } else {
            ((value - from.first) * ratio).toInt() + to.first
        }
    }
    return targetArray
}

fun linearRescaleToInt(
    array: LongArray,
    offset: Int,
    size: Int,
    from: LongRange,
    to: IntRange,
    targetArray: IntArray = IntArray(array.size),
): IntArray {
    val ratio = (to.last - to.first).toDouble() / (from.last - from.first)
    for (index in offset until offset + size) {
        val value = array[index]
        targetArray[index] = if (value <= from.first) {
            to.first
        } else if (value >= from.last) {
            to.last
        } else {
            ((value - from.first) * ratio).toInt() + to.first
        }
    }
    return targetArray
}

fun linearRescaleToInt(
    array: FloatArray,
    offset: Int,
    size: Int,
    from: ClosedFloatingPointRange<Float>,
    to: IntRange,
    targetArray: IntArray = IntArray(array.size),
): IntArray {
    val ratio = (to.last - to.first).toDouble() / (from.start - from.endInclusive)
    for (index in offset until offset + size) {
        val value = array[index]
        targetArray[index] = if (value <= from.start) {
            to.first
        } else if (value >= from.endInclusive) {
            to.last
        } else {
            ((value - from.start) * ratio).toInt() + to.first
        }
    }
    return targetArray
}

fun linearRescaleToInt(
    array: DoubleArray,
    offset: Int,
    size: Int,
    from: ClosedFloatingPointRange<Double>,
    to: IntRange,
    targetArray: IntArray = IntArray(array.size),
): IntArray {
    val ratio = (to.last - to.first).toDouble() / (from.start - from.endInclusive)
    for (index in offset until offset + size) {
        val value = array[index]
        targetArray[index] = if (value <= from.start) {
            to.first
        } else if (value >= from.endInclusive) {
            to.last
        } else {
            ((value - from.start) * ratio).toInt() + to.first
        }
    }
    return targetArray
}

@Deprecated("replace to linearRescaleToInt")
fun <T, D> linearRescale(
    data: MultiArray<T, D>,
    from: NumberRange<T>,
    to: IntRange,
): NDArray<Int, D> where T : Number, T : Comparable<T>, D : Dimension {
    val dimension = data.dim
    val shape = data.shape

    val ratio = (to.last - to.first).toDouble() / from.gap

    val toStart = to.start
    val toEnd = to.endInclusive
    val targetArray = when (data.dtype) {
        DataType.IntDataType -> {
            val array = (data as MultiArray<Int, D>).toIntArray()
            val targetArray = IntArray(array.size)
            val fromStart = from.start.toInt()
            val fromEnd = from.endInclusive.toInt()
            for (i in array.indices) {
                targetArray[i] = if (array[i] <= fromStart) {
                    toStart
                } else if (array[i] >= fromEnd) {
                    toEnd
                } else {
                    ((array[i] - fromStart) * ratio).toInt() + toStart
                }
            }
            targetArray
        }

        DataType.LongDataType -> {
            val array = (data as MultiArray<Long, D>).toLongArray()
            val targetArray = IntArray(array.size)
            val fromStart = from.start.toLong()
            val fromEnd = from.endInclusive.toLong()
            for (i in array.indices) {
                targetArray[i] = if (array[i] <= fromStart) {
                    toStart
                } else if (array[i] >= fromEnd) {
                    toEnd
                } else {
                    ((array[i] - fromStart) * ratio).toInt() + toStart
                }
            }
            targetArray
        }

        DataType.FloatDataType -> {
            val array = (data as MultiArray<Float, D>).toFloatArray()
            val targetArray = IntArray(array.size)
            val fromStart = from.start.toFloat()
            val fromEnd = from.endInclusive.toFloat()
            for (i in array.indices) {
                targetArray[i] = if (array[i] <= fromStart) {
                    toStart
                } else if (array[i] >= fromEnd) {
                    toEnd
                } else {
                    ((array[i] - fromStart) * ratio).toInt() + toStart
                }
            }
            targetArray
        }

        DataType.DoubleDataType -> {
            val array = (data as MultiArray<Double, D>).toDoubleArray()
            val targetArray = IntArray(array.size)
            val fromStart = from.start.toDouble()
            val fromEnd = from.endInclusive.toDouble()
            for (i in array.indices) {
                targetArray[i] = if (array[i] <= fromStart) {
                    toStart
                } else if (array[i] >= fromEnd) {
                    toEnd
                } else {
                    ((array[i] - fromStart) * ratio).toInt() + toStart
                }
            }
            targetArray
        }

        else -> throw UnsupportedOperationException()
    }

    return NDArray(MemoryViewIntArray(targetArray), shape = shape, dim = dimension)
}
