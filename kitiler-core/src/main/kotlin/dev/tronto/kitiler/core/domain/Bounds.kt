package dev.tronto.kitiler.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class Bounds(val bounds: DoubleArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bounds

        return bounds.contentEquals(other.bounds)
    }

    override fun hashCode(): Int = bounds.contentHashCode()
}
