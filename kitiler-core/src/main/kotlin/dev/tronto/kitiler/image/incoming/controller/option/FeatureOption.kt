package dev.tronto.kitiler.image.incoming.controller.option

import org.locationtech.jts.geom.Polygon

data class FeatureOption(val polygon: Polygon, val crsString: String) : ImageOption
