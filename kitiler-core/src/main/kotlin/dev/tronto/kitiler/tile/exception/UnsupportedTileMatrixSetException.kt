package dev.tronto.kitiler.tile.exception

import dev.tronto.kitiler.core.exception.IllegalParameterException

class UnsupportedTileMatrixSetException(id: String) :
    IllegalParameterException(
        "TileMatrixSet $id is not supported."
    )
