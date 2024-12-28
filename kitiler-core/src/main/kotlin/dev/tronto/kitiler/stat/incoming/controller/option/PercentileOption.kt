package dev.tronto.kitiler.stat.incoming.controller.option

import dev.tronto.kitiler.stat.domain.Percentile

@JvmInline
value class PercentileOption(val percentiles: List<Percentile>) : StatisticsOption
