package dev.tronto.kitiler.core.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object ResourceContextHolder {
    private object ResourceContextFactory {
        private val logger = KotlinLogging.logger { }
        private val managerCount = AtomicInteger(0)
        private val managerQueue = ConcurrentLinkedQueue<ResourceContextImpl>()

        private class ResourceContextImpl : ResourceContext {
            init {
                managerCount.incrementAndGet()
            }
            private val onReleaseList: MutableList<() -> Unit> = mutableListOf()
            private val valuemap: MutableMap<Any, Any> = mutableMapOf()
            private var released: AtomicBoolean = AtomicBoolean(true)
            override fun <T : Any> get(key: Any): T? = valuemap.get(key) as? T

            override fun <T : Any> set(key: Any, value: T): T {
                valuemap.set(key, value)
                return get<T>(key) ?: value
            }

            override fun onRelease(block: () -> Unit) {
                onReleaseList.add(block)
            }

            fun startUse(): Boolean = released.getAndSet(false)

            fun release(): Boolean {
                if (!released.getAndSet(true)) {
                    onReleaseList.forEach {
                        runCatching {
                            it()
                        }.onFailure {
                            logger.warn(it) { "release failed." }
                        }
                    }
                    return true
                }
                return false
            }
        }

        fun create(): ResourceContext = (managerQueue.poll() ?: ResourceContextImpl()).also {
            it.startUse()
        }

        fun release(manager: ResourceContext) {
            if (manager is ResourceContextImpl) {
                if (manager.release()) {
                    managerQueue.add(manager)
                }
            }
        }
    }

    data class StartedContext(val manager: ResourceContext, private val isStarted: Boolean) :
        ResourceContext by manager {
        fun releaseIfStarted() {
            if (isStarted) {
                ResourceContextFactory.release(manager)
            }
        }
    }

    private val threadLocal = ThreadLocal<ResourceContext>()

    fun getOrStart(): StartedContext {
        val manager = threadLocal.get()
        return if (manager == null) {
            val newManager = ResourceContextFactory.create()
            threadLocal.set(newManager)
            StartedContext(newManager, true)
        } else {
            StartedContext(manager, false)
        }
    }

    fun getManagerOrNull(): ResourceContext? = threadLocal.get()

    internal fun setManager(manager: ResourceContext?) {
        if (manager == null) {
            threadLocal.remove()
        } else {
            threadLocal.set(manager)
        }
    }
}
