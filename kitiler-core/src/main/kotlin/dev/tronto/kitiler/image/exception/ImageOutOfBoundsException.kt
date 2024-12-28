package dev.tronto.kitiler.image.exception

import dev.tronto.kitiler.core.exception.DataNotExistsException
import dev.tronto.kitiler.image.domain.Window

open class ImageOutOfBoundsException(requested: Window, raster: Window) :
    DataNotExistsException(
        "Requested $requested is out of bounds of $raster"
    )
