package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.exception.UnsupportedCrsStringException
import dev.tronto.kitiler.core.outgoing.port.CRS
import dev.tronto.kitiler.core.outgoing.port.CRSFactory
import dev.tronto.kitiler.core.outgoing.port.CRSTransform
import org.gdal.osr.CoordinateTransformation
import org.gdal.osr.SpatialReference
import org.gdal.osr.osr

object SpatialReferenceCRSFactory : CRSFactory {
    private val spatialRefCache = mutableMapOf<String, SpatialReference>()

    override fun create(crsString: String): CRS {
        kotlin.runCatching {
            val spatialReference = spatialRefCache.getOrPut(crsString) {
                SpatialReference().apply {
                    SetFromUserInput(crsString)
                    AutoIdentifyEPSG()
                    this.SetAxisMappingStrategy(osr.OAMS_TRADITIONAL_GIS_ORDER)
                }
            }
            return SpatialReferenceCRS(spatialReference, crsString)
        }.getOrElse {
            throw UnsupportedCrsStringException(crsString, it)
        }
    }

    private val geoGraphicRefCache = mutableMapOf<SpatialReference, SpatialReference>()

    override fun createGeographicCRS(crs: CRS): CRS {
        val targetCrs = if (crs is SpatialReferenceCRS) {
            crs
        } else {
            create(crs.input) as SpatialReferenceCRS
        }
        val resultSrs = geoGraphicRefCache.getOrPut(targetCrs.srs) {
            targetCrs.srs.CloneGeogCS()
        }

        return SpatialReferenceCRS(resultSrs, crs.input)
    }

    private val transformationCache = mutableMapOf<Pair<String, String>, CoordinateTransformation>()
    override fun transformTo(source: CRS, destination: CRS): CRSTransform {
        if (source.proj4 == destination.proj4) {
            return CRSTransform.Empty
        }
        val sourceCRS = if (source is SpatialReferenceCRS) {
            source
        } else {
            create(source.proj4) as SpatialReferenceCRS
        }
        val destinationCRS = if (destination is SpatialReferenceCRS) {
            destination
        } else {
            create(destination.proj4) as SpatialReferenceCRS
        }
        val transform = transformationCache.getOrPut(sourceCRS.proj4 to destinationCRS.proj4) {
            CoordinateTransformation(sourceCRS.srs, destinationCRS.srs)
        }
        return SpatialReferenceCRSTransform(transform)
    }
}
