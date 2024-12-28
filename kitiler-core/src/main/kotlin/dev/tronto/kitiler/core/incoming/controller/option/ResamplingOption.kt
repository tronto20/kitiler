package dev.tronto.kitiler.core.incoming.controller.option

import dev.tronto.kitiler.core.domain.ResamplingAlgorithm

@JvmInline
value class ResamplingOption(val algorithm: ResamplingAlgorithm) : OpenOption
