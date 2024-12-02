package dev.tronto.titiler.image.outgoing.port

import dev.tronto.titiler.core.domain.DataType
import org.locationtech.jts.geom.Geometry

interface ImageData {
    val dataType: DataType
    val band: Int
    val width: Int
    val height: Int

    val masked: Boolean

    fun <T> rescaleToUInt8(rangeFrom: List<ClosedRange<T>>): ImageData where T : Number, T : Comparable<T> = rescale(
        rangeFrom,
        listOf(0..255),
        DataType.UInt8
    )

    fun <T, R> rescale(
        rangeFrom: List<ClosedRange<T>>,
        rangeTo: List<ClosedRange<R>>,
        dataType: DataType,
    ): ImageData where T : Number, R : Number, T : Comparable<T>, R : Comparable<R>

    fun mask(geom: Geometry): ImageData
}