package dev.tronto.kitiler.core.exception

class UnsupportedCrsStringException(crsString: String) :
    dev.tronto.kitiler.core.exception.IllegalParameterException(
        "Cannot create SpatialReference. Unsupported crsString : $crsString"
    )
