package dev.tronto.kitiler.core.exception

class UnsupportedCrsStringException(crsString: String, cause: Throwable? = null) :
    IllegalParameterException(
        "Cannot create SpatialReference. Unsupported crsString : $crsString",
        cause
    )
