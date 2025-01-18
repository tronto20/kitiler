package dev.tronto.kitiler.core.outgoing.port

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry

interface CoordinateTransform {
    object Empty : CoordinateTransform {
        override fun <T : Geometry> transform(geometry: T): T = geometry

        override fun <T : Geometry> inverse(geometry: T): T = geometry

        override fun <T : Coordinate> transform(coordinate: T): T = coordinate

        override fun <T : Coordinate> inverse(coordinate: T): T = coordinate
    }

    fun <T : Geometry> transform(geometry: T): T

    fun <T : Coordinate> transform(coordinate: T): T

    fun <T : Geometry> inverse(geometry: T): T

    fun <T : Coordinate> inverse(coordinate: T): T
}
