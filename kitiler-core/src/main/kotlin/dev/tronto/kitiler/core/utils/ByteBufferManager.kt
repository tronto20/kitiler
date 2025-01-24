package dev.tronto.kitiler.core.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ByteBufferManager {
    private val logger = KotlinLogging.logger { }
    private val bufferMap: MutableMap<Int, MutableList<ByteBuffer>> = mutableMapOf()
    private val sizeUsed: MutableSet<Int> = mutableSetOf()
    private var nextTarget: Set<Int> = setOf()
    private var count = 0

    // band * height * width * dataSize (Int size) * scale average (1, 2, 4)
    private const val EXPECT_SIZE = 4 * 256 * 256 * 4 * 3
    private const val USE_PER_CORE = 3
    private val DIRECT_ENABLED: Boolean
    private val CLEAR_THRESHOLD: Int

    init {
        val disableDirectMemory = System.getProperty("memory.direct.disable")?.equals("true", true) == true
        DIRECT_ENABLED = if (disableDirectMemory) {
            false
        } else {
            (ApplicationContext.directMemory > EXPECT_SIZE * (ApplicationContext.core * USE_PER_CORE)).also {
                if (!it) {
                    logger.warn {
                        "Direct memory access is disabled cause direct memory is insufficient. " +
                            "Use -XX:MaxDirectMemorySize option for enable Direct memory access."
                    }
                }
            }
        }
        CLEAR_THRESHOLD = if (DIRECT_ENABLED) {
            (ApplicationContext.directMemory / USE_PER_CORE / EXPECT_SIZE).toInt()
        } else {
            (ApplicationContext.memory / USE_PER_CORE / EXPECT_SIZE).toInt()
        }
    }

    fun get(size: Int): ByteBuffer {
        val buffer = bufferMap[size]?.firstOrNull()
            ?: if (DIRECT_ENABLED) {
                ByteBuffer.allocateDirect(size)
            } else {
                ByteBuffer.allocate(size)
            }.order(ByteOrder.nativeOrder())

        logger.trace { "create buffer" }
        ResourceManagerHolder.getManagerOrNull()?.onRelease { release(buffer) }
        return buffer
    }

    fun release(buffer: ByteBuffer) {
        CoroutineScope(Dispatchers.SingleThread).launch {
            buffer.clear()
            val size = buffer.limit()
            val list = bufferMap.computeIfAbsent(size) { mutableListOf() }
            list.add(buffer)
            sizeUsed.add(size)
            if (count++ > CLEAR_THRESHOLD) {
                clear()
            }
            logger.trace { "release buffer" }
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
