package dev.tronto.titiler.image.outgoing.adaptor.multik

class NumberRange<T>(
    private val delegate: ClosedRange<T>,
) : ClosedRange<T> where T : Comparable<T>, T : Number {
    val gap: Double
        get() = endInclusive.toDouble() - start.toDouble()
    override val endInclusive: T
        get() = delegate.endInclusive
    override val start: T
        get() = delegate.start
}