package dev.tronto.kitiler.spring.application.image

import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.image.domain.Image
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.ImageFormat
import org.locationtech.jts.geom.Geometry

val testImageData: ImageData = object : ImageData {
    override val dataType: DataType
        get() = DataType.UInt16
    override val band: Int
        get() = 3
    override val width: Int
        get() = 256
    override val height: Int
        get() = 256
    override val masked: Boolean
        get() = true

    override suspend fun <T, R> rescale(rangeFrom: List<ClosedRange<T>>, rangeTo: List<ClosedRange<R>>, dataType: DataType): ImageData where R : Number, T : Number, T : Comparable<T>, R : Comparable<R> = throw NotImplementedError()

    override fun mask(geom: Geometry): ImageData = throw NotImplementedError()
}

val testImage: Image = object : Image {
    override val data: ByteArray
        get() = "test image".toByteArray(Charsets.UTF_8)
    override val format: ImageFormat
        get() = object : ImageFormat {
            override val name: String = "text"
            override val contentType: String = "text/plain"
        }
}
