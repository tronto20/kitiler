package dev.tronto.kitiler.core.utils

import io.github.oshai.kotlinlogging.KotlinLogging

object ResourceManagerHolder {
    private object ResourceManagerFactory {
        private val logger = KotlinLogging.logger { }

        private class ResourceManagerImpl(private val onReleaseList: MutableList<() -> Unit> = mutableListOf()) :
            ResourceManager {
            override fun onRelease(block: () -> Unit) {
                onReleaseList.add(block)
            }

            fun release() {
                onReleaseList.forEach {
                    runCatching {
                        it()
                    }.onFailure {
                        logger.warn(it) { "release failed." }
                    }
                }
            }
        }

        fun create(): ResourceManager = ResourceManagerImpl()
        fun release(manager: ResourceManager) {
            if (manager is ResourceManagerImpl) {
                manager.release()
            }
        }
    }

    data class StartedManager(val manager: ResourceManager, private val isStarted: Boolean) :
        ResourceManager by manager {
        fun releaseIfStarted() {
            if (isStarted) {
                ResourceManagerFactory.release(manager)
            }
        }
    }

    private val threadLocal = ThreadLocal<ResourceManager>()

    fun getOrStart(): StartedManager {
        val manager = threadLocal.get()
        return if (manager == null) {
            val newManager = ResourceManagerFactory.create()
            threadLocal.set(newManager)
            StartedManager(newManager, true)
        } else {
            StartedManager(manager, false)
        }
    }

    fun getManagerOrNull(): ResourceManager? = threadLocal.get()

    internal fun setManager(manager: ResourceManager?) {
        if (manager == null) {
            threadLocal.remove()
        } else {
            threadLocal.set(manager)
        }
    }
}
