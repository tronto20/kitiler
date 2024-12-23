package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.outgoing.port.CRS
import org.gdal.osr.SpatialReference
import org.gdal.osr.osr
import java.net.URI

class SpatialReferenceCRS(val srs: SpatialReference, override val input: String) : CRS {
    override val name: String = srs.GetName()
    override val semiMajor: Double = srs.GetSemiMajor()
    override val semiMinor: Double = srs.GetSemiMinor()
    override val invertAxis: Boolean by lazy {
        srs.GetAxisOrientation("GEOGCS", 0) in listOf(
            osr.OAO_Down,
            osr.OAO_Up,
            osr.OAO_North,
            osr.OAO_South
        )
    }
    override val wkt: String = srs.ExportToWkt()
    override val proj4: String = srs.ExportToProj4()
    override val unit: String = srs.GetAttrValue("unit") ?: "metre"
    override val uri: URI
        get() {
            val name = srs.GetAuthorityName(null)
            val code = srs.GetAuthorityCode(null)
            return if (name.contains("_")) {
                val (authority, version) = name.split('_')
                URI.create("http://www.opengis.net/def/crs/$authority/$version/$code")
            } else {
                URI.create("http://www.opengis.net/def/crs/$name/0/$code")
            }
        }
    override val epsgCode: Int
        get() = srs.GetAuthorityCode(null).toIntOrNull() ?: 0

    override fun isSame(other: CRS): Boolean {
        if (other is SpatialReferenceCRS) {
            return srs.IsSame(other.srs) == 1
        } else {
            val otherSrs = SpatialReference().apply {
                SetFromUserInput(other.input)
            }
            try {
                return srs.IsSame(otherSrs) == 1
            } finally {
                // ignore
                try {
                    otherSrs.delete()
                } catch (e: RuntimeException) {
                    // ignore
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is CRS) return isSame(other)
        return false
    }

    override fun hashCode(): Int = epsgCode.hashCode()

    override fun toString(): String = "SpatialReferenceCRS(epsg=$epsgCode)"
}
