package dev.tronto.kitiler.core.utils

import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class ResourceManagerContext(private val manager: ResourceManager? = ResourceManagerHolder.getManagerOrNull()) :
    AbstractCoroutineContextElement(Key),
    ThreadContextElement<ResourceManager?> {
    companion object Key : CoroutineContext.Key<ResourceManagerContext>

    override fun restoreThreadContext(context: CoroutineContext, oldState: ResourceManager?) {
        ResourceManagerHolder.setManager(oldState)
    }

    override fun updateThreadContext(context: CoroutineContext): ResourceManager? {
        val oldState = ResourceManagerHolder.getManagerOrNull()
        ResourceManagerHolder.setManager(manager)
        return oldState
    }
}
