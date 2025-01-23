package dev.tronto.kitiler.core.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.management.ManagementFactory

object ApplicationContext {
    private val logger = KotlinLogging.logger { }
    val core = Runtime.getRuntime().availableProcessors()
    val memory = Runtime.getRuntime().maxMemory()
    val directMemory: Long

    init {
        val pattern = "\\s*-XX:MaxDirectMemorySize\\s*=\\s*([0-9]+)\\s*([kKmMgG]?)\\s*$".toPattern()
        val vmArgs = ManagementFactory.getRuntimeMXBean().inputArguments
        directMemory = vmArgs.asReversed().asSequence().map {
            pattern.matcher(it)
        }.find { it.matches() }?.let {
            val memory = it.group(1).toLong()
            when (it.group(2)[0]) {
                'k', 'K' -> memory * 1024
                'm', 'M' -> memory * 1024 * 1024
                'g', 'G' -> memory * 1024 * 1024 * 1024
                else -> memory
            }
        } ?: memory
        logger.info { "Using direct memory: $directMemory, heap memory: $memory, core: $core" }
    }
}
