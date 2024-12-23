package dev.tronto.kitiler.document.template

import java.text.NumberFormat
import java.util.*

class Formats(private val locale: Locale) {
    private val defaultFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH).apply {
        isGroupingUsed = false
        maximumIntegerDigits = 40
        maximumFractionDigits = 40
    }

    @JvmOverloads
    fun number(target: Number?, maximumDigits: Int = 40, useGrouping: Boolean = false): String? {
        val format = if (maximumDigits == 40 && useGrouping == false) {
            defaultFormat
        } else {
            NumberFormat.getNumberInstance(locale).apply {
                isGroupingUsed = useGrouping
                maximumIntegerDigits = maximumDigits
                maximumFractionDigits = maximumDigits
            }
        }
        return when (target) {
            null -> null
            is Long -> format.format(target)
            is Double -> format.format(target)
            else -> format.format(target)
        }
    }
}
