package dev.tronto.kitiler.stat.domain

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Percentile(val value: Int) : Comparable<Percentile> {

    init {
        require(value >= 0) {
            "Percentile must be >= 0"
        }
        require(value <= 100) {
            "Percentile must be <= 100"
        }
    }

    override fun compareTo(other: Percentile): Int = value.compareTo(other.value)

    override fun toString(): String = value.toString()
}
