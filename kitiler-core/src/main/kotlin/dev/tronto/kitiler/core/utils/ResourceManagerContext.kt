package dev.tronto.kitiler.core.utils

import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class ResourceManagerContext(
    private val startedManager: ResourceManagerHolder.StartedManager = ResourceManagerHolder.getOrStart(),
) : AbstractCoroutineContextElement(Key),
    ThreadContextElement<ResourceManager?> {
    companion object Key : CoroutineContext.Key<ResourceManagerContext>

    override fun restoreThreadContext(context: CoroutineContext, oldState: ResourceManager?) {
        ResourceManagerHolder.setManager(oldState)
        startedManager.releaseIfStarted()
    }

    override fun updateThreadContext(context: CoroutineContext): ResourceManager? {
        val oldState = ResourceManagerHolder.getManagerOrNull()
        ResourceManagerHolder.setManager(startedManager.manager)
        return oldState
    }
}
