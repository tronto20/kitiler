package dev.tronto.kitiler.core.outgoing.adaptor.jts

import dev.tronto.kitiler.core.outgoing.port.CoordinateTransform
import org.locationtech.jts.geom.util.AffineTransformation

@Suppress("FunctionName")
fun MoveCoordinateTransform(x: Double, y: Double): CoordinateTransform =
    AffineCoordinateTransform(AffineTransformation.translationInstance(x, y))
