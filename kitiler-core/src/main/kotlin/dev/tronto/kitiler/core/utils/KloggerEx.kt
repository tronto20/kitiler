package dev.tronto.kitiler.core.utils

import io.github.oshai.kotlinlogging.KLogger
import kotlin.time.TimeSource

inline fun <T> KLogger.logTrace(name: String, block: () -> T): T = if (this.isTraceEnabled()) {
    val mark = TimeSource.Monotonic.markNow()
    trace { "start $name" }
    try {
        block()
    } finally {
        trace { "end $name - ${mark.elapsedNow()}" }
    }
} else {
    block()
}
