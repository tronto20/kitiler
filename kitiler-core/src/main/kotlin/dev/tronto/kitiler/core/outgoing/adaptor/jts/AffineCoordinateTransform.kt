package dev.tronto.kitiler.core.outgoing.adaptor.jts

import dev.tronto.kitiler.core.outgoing.port.CoordinateTransform
import org.locationtech.jts.geom.CoordinateXY
import org.locationtech.jts.geom.util.AffineTransformation

@JvmInline
value class AffineCoordinateTransform(val affine: AffineTransformation) : CoordinateTransform {
    override fun transformTo(coord: CoordinateXY): CoordinateXY {
        val dst = CoordinateXY(coord.x, coord.y)
        affine.transform(coord, dst)
        return dst
    }

    override fun inverse(coord: CoordinateXY): CoordinateXY {
        val dst = CoordinateXY(coord.x, coord.y)
        affine.inverse.transform(coord, dst)
        return dst
    }
}
