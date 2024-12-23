package dev.tronto.kitiler.core.outgoing.port

import org.locationtech.jts.geom.CoordinateXY
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.Polygon

interface CoordinateTransform {
    object Empty : CoordinateTransform {
        override fun transformTo(coord: CoordinateXY): CoordinateXY = coord

        override fun inverse(coord: CoordinateXY): CoordinateXY = coord
    }

    fun transformTo(coord: CoordinateXY): CoordinateXY

    fun transformTo(linearRing: LinearRing): LinearRing = linearRing.factory.createLinearRing(
        linearRing.coordinates.map {
            transformTo(CoordinateXY(it))
        }.toTypedArray()
    )

    fun transformTo(polygon: Polygon): Polygon {
        val exteriorRing = transformTo(polygon.exteriorRing)
        val interiorRings = (0..<polygon.numInteriorRing).map { interiorRingIndex ->
            transformTo(polygon.getInteriorRingN(interiorRingIndex))
        }

        return polygon.factory.createPolygon(exteriorRing, interiorRings.toTypedArray())
    }

    fun inverse(coord: CoordinateXY): CoordinateXY

    fun inverse(linearRing: LinearRing): LinearRing = linearRing.factory.createLinearRing(
        linearRing.coordinates.map {
            inverse(CoordinateXY(it))
        }.toTypedArray()
    )

    fun inverse(polygon: Polygon): Polygon {
        val exteriorRing = inverse(polygon.exteriorRing)
        val interiorRings = (0..<polygon.numInteriorRing).map { interiorRingIndex ->
            inverse(polygon.getInteriorRingN(interiorRingIndex))
        }

        return polygon.factory.createPolygon(exteriorRing, interiorRings.toTypedArray())
    }
}
