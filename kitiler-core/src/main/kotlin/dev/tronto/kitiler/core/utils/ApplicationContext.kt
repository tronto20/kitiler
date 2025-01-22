package dev.tronto.kitiler.core.utils

object ApplicationContext {
    val core = Runtime.getRuntime().availableProcessors()
    val memory = Runtime.getRuntime().maxMemory()
}
