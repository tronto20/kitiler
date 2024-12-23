package dev.tronto.kitiler.core.outgoing.port

import java.net.URI

interface CRS {
    val name: String
    val wkt: String
    val proj4: String
    val unit: String
    val semiMajor: Double
    val semiMinor: Double
    val invertAxis: Boolean
    val uri: URI
    val epsgCode: Int
    val input: String

    fun isSame(other: CRS): Boolean
}
