package dev.tronto.kitiler.core.domain

interface Ordered {
    val order: Int
        get() = 0
}
