package dev.tronto.kitiler.image.incoming.controller.option

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption

@JvmInline
value class BandIndexOption(val bandIndexes: List<BandIndex>) : OpenOption {
    init {
        require(bandIndexes.isNotEmpty()) {
            "BandIndexOption cannot be empty"
        }
    }
}
