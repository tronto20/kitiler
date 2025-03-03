package dev.tronto.kitiler.core.utils

import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class ResourceManagerContext(private val manager: ResourceContext? = ResourceContextHolder.getManagerOrNull()) :
    AbstractCoroutineContextElement(Key),
    ThreadContextElement<ResourceContext?> {
    companion object Key : CoroutineContext.Key<ResourceManagerContext>

    override fun restoreThreadContext(context: CoroutineContext, oldState: ResourceContext?) {
        ResourceContextHolder.setManager(oldState)
    }

    override fun updateThreadContext(context: CoroutineContext): ResourceContext? {
        val oldState = ResourceContextHolder.getManagerOrNull()
        ResourceContextHolder.setManager(manager)
        return oldState
    }
}
