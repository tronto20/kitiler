package dev.tronto.kitiler.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface ResourceManager {
    fun onRelease(block: () -> Unit)
}

suspend fun <T> withResourceManager(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T,
): T {
    val startedManager = ResourceManagerHolder.getOrStart()
    return withContext(context + ResourceManagerContext(), block).also {
        startedManager.releaseIfStarted()
    }
}
