package dev.tronto.kitiler.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

object ByteBufferManager {
    private val bufferMap: MutableMap<Int, MutableList<ByteBuffer>> = mutableMapOf()
    private val sizeUsed: MutableSet<Int> = mutableSetOf()
    private var nextTarget: Set<Int> = setOf()
    private var count = 0

    // band * height * width * dataSize (Int size) * scale average (1, 2, 4)
    private const val EXPECT_SIZE = 4 * 256 * 256 * 4 * 3
    private val CLEAR_THRESHOLD = (ApplicationContext.memory / 3 / EXPECT_SIZE).toInt()

    fun get(size: Int): ByteBuffer = bufferMap[size]?.firstOrNull() ?: ByteBuffer.allocateDirect(size)

    fun release(buffer: ByteBuffer) {
        if (!buffer.isDirect) return
        CoroutineScope(Dispatchers.SingleThread).launch {
            buffer.clear()
            val size = buffer.limit()
            val list = bufferMap.computeIfAbsent(size) { mutableListOf() }
            list.add(buffer)
            sizeUsed.add(size)
            if (count++ > CLEAR_THRESHOLD) {
                clear()
            }
        }
    }

    private fun clear() {
        val target = nextTarget - sizeUsed
        target.forEach {
            bufferMap.remove(it)
        }
        nextTarget = bufferMap.keys - sizeUsed
        sizeUsed.clear()
        count = 0
    }
}
