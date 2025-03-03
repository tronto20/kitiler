package dev.tronto.kitiler.image.outgoing.adaptor.gdal

import dev.tronto.kitiler.image.domain.DataBuffer
import dev.tronto.kitiler.image.domain.IntArrayDataBuffer

internal fun BooleanArray.toDataBuffer(validValue: Int = 255): DataBuffer = IntArrayDataBuffer(
    IntArray(this.size) {
        if (this[it]) validValue else 0
    }
)
