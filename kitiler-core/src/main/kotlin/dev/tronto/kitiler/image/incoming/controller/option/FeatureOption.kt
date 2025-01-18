package dev.tronto.kitiler.image.incoming.controller.option

import org.locationtech.jts.geom.Geometry

data class FeatureOption(val geometry: Geometry, val crsString: String) : ImageOption
