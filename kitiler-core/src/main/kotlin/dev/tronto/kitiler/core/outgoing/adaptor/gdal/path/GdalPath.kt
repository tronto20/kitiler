package dev.tronto.kitiler.core.outgoing.adaptor.gdal.path

import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import java.net.URI

interface GdalPath {
    fun toURI(): URI
    fun toPathString(): String
    val openOptions: OptionProvider<OpenOption>
}
