package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.outgoing.port.CRSTransform
import org.gdal.osr.CoordinateTransformation
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.CoordinateSequence
import org.locationtech.jts.geom.CoordinateSequenceFilter
import org.locationtech.jts.geom.Geometry

class SpatialReferenceCRSTransform(private val transform: CoordinateTransformation) : CRSTransform {
    private class CoordinateTransformAdaptor(private val transform: CoordinateTransformation) :
        CoordinateSequenceFilter {
        override fun filter(coordinateSequence: CoordinateSequence, index: Int) {
            val (x, y) = transform.TransformPoint(
                coordinateSequence.getOrdinate(index, 0),
                coordinateSequence.getOrdinate(index, 1)
            )
            coordinateSequence.setOrdinate(index, 0, x)
            coordinateSequence.setOrdinate(index, 1, y)
        }
        override fun isDone(): Boolean = false

        override fun isGeometryChanged(): Boolean = true
    }

    private val adaptor = CoordinateTransformAdaptor(transform)

    private val inverse = transform.GetInverse()
    private val inverseAdaptor = CoordinateTransformAdaptor(inverse)
    override fun <T : Geometry> inverse(geometry: T): T {
        val g = geometry.copy()
        g.apply(inverseAdaptor)
        @Suppress("UNCHECKED_CAST")
        return g as T
    }

    override fun <T : Geometry> transform(geometry: T): T {
        val g = geometry.copy()
        g.apply(adaptor)
        @Suppress("UNCHECKED_CAST")
        return g as T
    }

    override fun <T : Coordinate> transform(coordinate: T): T {
        val c = coordinate.copy()
        val (x, y) = transform.TransformPoint(c.x, c.y)
        c.x = x
        c.y = y
        @Suppress("UNCHECKED_CAST")
        return c as T
    }

    override fun <T : Coordinate> inverse(coordinate: T): T {
        val c = coordinate.copy()
        val (x, y) = inverse.TransformPoint(c.x, c.y)
        c.x = x
        c.y = y
        @Suppress("UNCHECKED_CAST")
        return c as T
    }
}
