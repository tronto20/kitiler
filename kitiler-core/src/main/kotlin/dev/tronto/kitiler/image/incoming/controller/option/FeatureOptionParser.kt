package dev.tronto.kitiler.image.incoming.controller.option

import dev.tronto.kitiler.core.exception.RequiredParameterMissingException
import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.OptionDescription
import dev.tronto.kitiler.core.incoming.controller.option.OptionParser
import dev.tronto.kitiler.core.incoming.controller.option.Request
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.SpatialReferenceCRSFactory
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import org.locationtech.jts.io.geojson.GeoJsonConstants
import org.locationtech.jts.io.geojson.GeoJsonReader
import org.locationtech.jts.io.geojson.GeoJsonWriter

class FeatureOptionParser : OptionParser<FeatureOption> {
    companion object {
        private const val DEFAULT_CRS = "EPSG:4326"
        private const val PARAM = "feature"
        private const val CRS_PARAM = "featureCrs"
    }

    override val type: ArgumentType<FeatureOption> = ArgumentType()

    override fun generateMissingException(): Exception = RequiredParameterMissingException(PARAM)

    override suspend fun parse(request: Request): FeatureOption? =
        request.parameter(PARAM).firstOrNull()?.let { geometryString ->
            val crsString = request.parameter(CRS_PARAM).firstOrNull() ?: DEFAULT_CRS
            val crs = SpatialReferenceCRSFactory.create(crsString)
            val reader = GeoJsonReader(GeometryFactory(PrecisionModel(), crs.epsgCode))
            val geometry = reader.read(geometryString)
            FeatureOption(geometry, crsString)
        }

    override fun box(option: FeatureOption): Map<String, List<String>> {
        val writer = GeoJsonWriter()
        val geometryString = writer.write(option.geometry)

        return mapOf(PARAM to listOf(geometryString), CRS_PARAM to listOf(option.crsString))
    }

    override fun descriptions(): List<OptionDescription<*>> {
        val crs = SpatialReferenceCRSFactory.create(DEFAULT_CRS)
        val factory = GeometryFactory(PrecisionModel(), crs.epsgCode)
        val samplePolygon = factory.createPolygon(
            arrayOf(
                Coordinate(0.0, 0.0),
                Coordinate(1.0, 0.0),
                Coordinate(1.0, 1.0),
                Coordinate(0.0, 1.0),
                Coordinate(0.0, 0.0)
            )
        )

        val geometryTypes = listOf(
            GeoJsonConstants.NAME_POLYGON,
            GeoJsonConstants.NAME_MULTIPOLYGON,
            GeoJsonConstants.NAME_GEOMETRYCOLLECTION,
            GeoJsonConstants.NAME_FEATURE,
            GeoJsonConstants.NAME_FEATURECOLLECTION
        )
        return listOf(
            OptionDescription<String>(
                PARAM,
                "feature. one of ${geometryTypes.joinToString { "'$it'" }}",
                GeoJsonWriter().write(samplePolygon)
            ),
            OptionDescription<String>(
                CRS_PARAM,
                "feature crsString.",
                DEFAULT_CRS,
                default = DEFAULT_CRS
            )
        )
    }
}
