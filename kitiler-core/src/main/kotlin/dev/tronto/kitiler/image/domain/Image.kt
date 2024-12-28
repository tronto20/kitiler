package dev.tronto.kitiler.image.domain

interface Image {
    val data: ByteArray
    val format: ImageFormat
}
