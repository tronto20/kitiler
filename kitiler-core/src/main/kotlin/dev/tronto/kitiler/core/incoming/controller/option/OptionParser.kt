package dev.tronto.kitiler.core.incoming.controller.option

import dev.tronto.kitiler.core.domain.Ordered
import java.util.*

interface OptionParser<T : Option> {
    val type: ArgumentType<T>
    fun generateMissingException(): Exception

    suspend fun parse(request: Request): T?
    fun box(option: T): Map<String, List<String>>
    fun descriptions(): List<OptionDescription<*>>

    companion object {
        @JvmStatic
        val services by lazy {
            ServiceLoader.load(OptionParser::class.java, Thread.currentThread().contextClassLoader)
                .toList()
                .sortedBy {
                    when (it) {
                        is Ordered -> it.order
                        else -> 0
                    }
                }
        }
    }
}
