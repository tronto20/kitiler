package dev.tronto.kitiler.stat.outgoing.port.spi

import dev.tronto.kitiler.core.domain.Ordered
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.stat.domain.BandStatistics
import dev.tronto.kitiler.stat.domain.Percentile
import java.util.*

interface ImageDataStatistics {
    fun supports(imageData: ImageData): Boolean
    suspend fun statistics(imageData: ImageData, percentiles: List<Percentile>): List<BandStatistics>

    companion object {
        @JvmStatic
        val services by lazy {
            ServiceLoader.load(
                ImageDataStatistics::class.java,
                Thread.currentThread().contextClassLoader
            ).toList().sortedBy { if (it is Ordered) it.order else 0 }
        }
    }
}
