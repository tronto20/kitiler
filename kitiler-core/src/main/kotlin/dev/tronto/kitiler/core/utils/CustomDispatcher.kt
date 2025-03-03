package dev.tronto.kitiler.core.utils

import kotlinx.coroutines.Dispatchers

private val SingleThreadDispatcher = Dispatchers.Default.limitedParallelism(1)

internal val Dispatchers.SingleThread
    get() = SingleThreadDispatcher
