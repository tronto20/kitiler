package dev.tronto.kitiler.document.domain

import dev.tronto.kitiler.core.domain.OptionContext

data class SimpleDocument(override val contents: String, override val format: DocumentFormat) :
    Document,
    OptionContext by OptionContext.wrap()
