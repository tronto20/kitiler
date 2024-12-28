package dev.tronto.kitiler.image.spi

import dev.tronto.kitiler.core.domain.Ordered
import dev.tronto.kitiler.image.domain.ImageFormat
import java.util.*

fun interface ImageFormatRegistrar {
    fun imageFormats(): Iterable<ImageFormat>

    companion object {
        @JvmStatic
        val services by lazy {
            ServiceLoader.load(ImageFormatRegistrar::class.java, Thread.currentThread().contextClassLoader)
                .sortedBy { if (it is Ordered) it.order else 0 }.toList()
        }
    }
}
