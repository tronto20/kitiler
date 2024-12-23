package dev.tronto.kitiler.image.domain

import org.locationtech.jts.geom.Envelope

data class Window(val xOffset: Int, val yOffset: Int, val width: Int, val height: Int) {
    fun toEnvelope(): Envelope = Envelope(
        (xOffset).toDouble(),
        (xOffset + width).toDouble(),
        yOffset.toDouble(),
        (yOffset + height).toDouble()
    )

    companion object {
        fun fromEnvelope(envelope: Envelope): Window = Window(
            envelope.minX.toInt(),
            envelope.minY.toInt(),
            (envelope.maxX - envelope.minX).toInt(),
            (envelope.maxY - envelope.minY).toInt()
        )
    }
}
