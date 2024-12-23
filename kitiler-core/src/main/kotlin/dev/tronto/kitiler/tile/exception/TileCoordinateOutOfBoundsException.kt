package dev.tronto.kitiler.tile.exception

import dev.tronto.kitiler.core.exception.IllegalParameterException

class TileCoordinateOutOfBoundsException(message: String) : IllegalParameterException(message) {
    constructor(
        position: String,
        value: Int,
        range: IntRange,
    ) : this("Tile Coordinate out of bounds. $position must in $range, but $value")

    constructor(
        position: String,
        value: Int,
        values: Iterable<Int>,
    ) : this("Tile Coordinate out of bounds. $position must in $values, but $value")
}
