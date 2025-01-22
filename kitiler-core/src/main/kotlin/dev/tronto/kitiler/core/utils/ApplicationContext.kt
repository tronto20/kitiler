package dev.tronto.kitiler.core.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.management.BufferPoolMXBean
import java.lang.management.ManagementFactory

object ApplicationContext {
    private val logger = KotlinLogging.logger { }
    val core = Runtime.getRuntime().availableProcessors()
    val memory = Runtime.getRuntime().maxMemory()
    val directMemory = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean::class.java).find {
        it.name == "direct"
    }?.totalCapacity ?: 0

    init {
        logger.info { "Using direct memory: $directMemory, heap memory: $memory, core: $core" }
    }
}
