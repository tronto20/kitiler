package dev.tronto.kitiler.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ArrayManager {
    private class Manager<T>(
        dataSize: Int,
        private val init: (Int) -> T,
        private val clear: (T) -> Unit,
        private val getSize: (T) -> Int,
    ) {
        private val arraymap: MutableMap<Int, MutableList<T>> = mutableMapOf()
        private val sizeUsed: MutableSet<Int> = mutableSetOf()
        private var nextTarget: Set<Int> = setOf()
        private var count = 0

        // band * height * width * dataSize * scale average (1, 2, 4)
        @Suppress("PrivatePropertyName")
        private val EXPECT_SIZE = 4 * 256 * 256 * dataSize * 3

        @Suppress("PrivatePropertyName")
        private val CLEAR_THRESHOLD = (ApplicationContext.memory / 10 / EXPECT_SIZE).toInt()

        fun get(size: Int): T = arraymap[size]?.firstOrNull() ?: init(size)

        fun release(array: T) {
            CoroutineScope(Dispatchers.SingleThread).launch {
                clear(array)
                val size = getSize(array)
                val list = arraymap.computeIfAbsent(size) { mutableListOf() }
                list.add(array)
                sizeUsed.add(size)
                if (count++ > CLEAR_THRESHOLD) {
                    clear()
                }
            }
        }

        private fun clear() {
            val target = nextTarget - sizeUsed
            target.forEach {
                arraymap.remove(it)
            }
            nextTarget = arraymap.keys - sizeUsed
            sizeUsed.clear()
            count = 0
        }
    }

    private val byteArrayManager = Manager<ByteArray>(
        Byte.SIZE_BYTES,
        { ByteArray(it) },
        { it.fill(0) },
        { it.size }
    )
    private val intArrayManager = Manager<IntArray>(
        Int.SIZE_BYTES,
        { IntArray(it) },
        { it.fill(0) },
        { it.size }
    )
    private val longArrayManager = Manager<LongArray>(
        Long.SIZE_BYTES,
        { LongArray(it) },
        { it.fill(0) },
        { it.size }
    )
    private val floatArrayManager = Manager<FloatArray>(
        Float.SIZE_BYTES,
        { FloatArray(it) },
        { it.fill(0f) },
        { it.size }
    )
    private val doubleArrayManager = Manager<DoubleArray>(
        Double.SIZE_BYTES,
        { DoubleArray(it) },
        { it.fill(.0) },
        { it.size }
    )

    fun getByteArray(size: Int): ByteArray = byteArrayManager.get(size)
    fun getIntArray(size: Int): IntArray = intArrayManager.get(size)
    fun getLongArray(size: Int): LongArray = longArrayManager.get(size)
    fun getFloatArray(size: Int): FloatArray = floatArrayManager.get(size)
    fun getDoubleArray(size: Int): DoubleArray = doubleArrayManager.get(size)

    fun release(array: ByteArray) = byteArrayManager.release(array)
    fun release(array: IntArray) = intArrayManager.release(array)
    fun release(array: LongArray) = longArrayManager.release(array)
    fun release(array: FloatArray) = floatArrayManager.release(array)
    fun release(array: DoubleArray) = doubleArrayManager.release(array)
}
