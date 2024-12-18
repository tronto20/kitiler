package dev.tronto.titiler.core.outgoing.adaptor.gdal.path

import dev.tronto.titiler.core.incoming.controller.option.OpenOption
import dev.tronto.titiler.core.incoming.controller.option.OptionProvider
import java.net.URI

suspend fun URI.tryToGdalPath(openOptions: OptionProvider<OpenOption>): GdalPath? =
    GdalPathProvider.of(this, openOptions)
