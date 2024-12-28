package dev.tronto.kitiler.image.incoming.controller.option

@JvmInline
value class RescaleOption(val rescale: List<ClosedRange<Double>>) : RenderOption
