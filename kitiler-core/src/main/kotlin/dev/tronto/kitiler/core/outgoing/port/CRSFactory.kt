package dev.tronto.kitiler.core.outgoing.port

interface CRSFactory {
    fun default(): CRS = create("EPSG:4326")
    fun create(crsString: String): CRS
    fun createGeographicCRS(crs: CRS): CRS
    fun transformTo(source: CRS, destination: CRS): CRSTransform
}
