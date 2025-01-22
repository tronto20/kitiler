package dev.tronto.kitiler.core.outgoing.adaptor.jts

import dev.tronto.kitiler.core.outgoing.port.CoordinateTransform
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.util.AffineTransformation

class AffineCoordinateTransform(val affine: AffineTransformation) : CoordinateTransform {
    private val inverse = affine.inverse
    override fun <T : Geometry> transform(geometry: T): T {
        @Suppress("UNCHECKED_CAST")
        return affine.transform(geometry) as T
    }

    override fun <T : Geometry> inverse(geometry: T): T {
        @Suppress("UNCHECKED_CAST")
        return inverse.transform(geometry) as T
    }

    override fun <T : Coordinate> transform(coordinate: T): T {
        val c = coordinate.copy()
        affine.transform(coordinate, c)
        @Suppress("UNCHECKED_CAST")
        return c as T
    }

    override fun <T : Coordinate> inverse(coordinate: T): T {
        val c = coordinate.copy()
        inverse.transform(coordinate, c)
        @Suppress("UNCHECKED_CAST")
        return c as T
    }
}
