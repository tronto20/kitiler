package dev.tronto.kitiler.tile.exception

import dev.tronto.kitiler.core.exception.DataNotExistsException
import dev.tronto.kitiler.tile.incoming.controller.option.TileCoordinateOption

class TileNotFoundException(val coordinate: TileCoordinateOption, cause: Throwable? = null) :
    DataNotExistsException(
        "Tile not found at z=${coordinate.z}, x=${coordinate.x}, y=${coordinate.y}",
        cause
    )
