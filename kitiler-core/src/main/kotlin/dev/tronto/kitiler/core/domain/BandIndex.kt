package dev.tronto.kitiler.core.domain

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class BandIndex(val value: Int) {
    init {
        require(value > 0) {
            "BandIndex must be positive"
        }
    }

    override fun toString(): String = value.toString()
}
