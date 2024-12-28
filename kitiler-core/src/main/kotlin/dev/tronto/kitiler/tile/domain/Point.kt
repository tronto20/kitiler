package dev.tronto.kitiler.tile.domain

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Point(val value: DoubleArray) {
    constructor(x: Number, y: Number) : this(doubleArrayOf(x.toDouble(), y.toDouble()))

    val x: Double
        get() = value[0]
    val y: Double
        get() = value[1]
}
