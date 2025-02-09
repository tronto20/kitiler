package dev.tronto.kitiler.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ResourceContext {
    fun <T : Any> get(key: Any): T?
    fun <T : Any> set(key: Any, value: T): T
    fun <T : Any> getOrSet(key: Any, value: () -> T): T = get(key) ?: set(key, value())

    fun onRelease(block: () -> Unit)
}

suspend fun <T> withResourceContext(block: suspend CoroutineScope.() -> T): T {
    val startedManager = ResourceContextHolder.getOrStart()
    return withContext(Dispatchers.Default + ResourceManagerContext(), block).also {
        startedManager.releaseIfStarted()
    }
}
