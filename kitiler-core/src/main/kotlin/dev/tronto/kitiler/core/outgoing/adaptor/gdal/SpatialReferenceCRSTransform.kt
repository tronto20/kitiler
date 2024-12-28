package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.outgoing.port.CRSTransform
import org.gdal.osr.CoordinateTransformation
import org.locationtech.jts.geom.CoordinateXY

class SpatialReferenceCRSTransform(val transform: CoordinateTransformation) : CRSTransform {
    val inverse = transform.GetInverse()
    override fun transformTo(coord: CoordinateXY): CoordinateXY {
        val (x, y) = transform.TransformPoint(coord.x, coord.y)
        return CoordinateXY(x, y)
    }

    override fun inverse(coord: CoordinateXY): CoordinateXY {
        val (x, y) = inverse.TransformPoint(coord.x, coord.y)
        return CoordinateXY(x, y)
    }
}
