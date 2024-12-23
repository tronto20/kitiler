package dev.tronto.kitiler.image.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.ResamplingAlgorithm
import org.gdal.gdal.Driver
import org.gdal.gdalconst.gdalconst

val ResamplingAlgorithm.gdalConst: Int
    get() =
        when (this) {
            ResamplingAlgorithm.NEAREST -> gdalconst.GRA_NearestNeighbour
            ResamplingAlgorithm.BILINEAR -> gdalconst.GRA_Bilinear
            ResamplingAlgorithm.CUBIC -> gdalconst.GRA_Cubic
            ResamplingAlgorithm.CUBIC_SPLINE -> gdalconst.GRA_CubicSpline
            ResamplingAlgorithm.LANCZOS -> gdalconst.GRA_Lanczos
            ResamplingAlgorithm.AVERAGE -> gdalconst.GRA_Average
            ResamplingAlgorithm.RMS -> gdalconst.GRA_RMS
            ResamplingAlgorithm.MODE -> gdalconst.GRA_Mode
        }

val ResamplingAlgorithm.gdalWarpString: String
    get() = when (this) {
        ResamplingAlgorithm.NEAREST -> "near"
        ResamplingAlgorithm.BILINEAR -> "bilinear"
        ResamplingAlgorithm.CUBIC -> "cubic"
        ResamplingAlgorithm.CUBIC_SPLINE -> "cubicspline"
        ResamplingAlgorithm.LANCZOS -> "lanczos"
        ResamplingAlgorithm.AVERAGE -> "average"
        ResamplingAlgorithm.RMS -> "rms"
        ResamplingAlgorithm.MODE -> "mode"
    }
val Driver.canCreate: Boolean
    get() = GetMetadataItem(gdalconst.DCAP_CREATE)?.uppercase() in listOf("YES", "TRUE")
val Driver.canCreateCopy: Boolean
    get() = GetMetadataItem(gdalconst.DCAP_CREATECOPY)?.uppercase() in listOf("YES", "TRUE")
