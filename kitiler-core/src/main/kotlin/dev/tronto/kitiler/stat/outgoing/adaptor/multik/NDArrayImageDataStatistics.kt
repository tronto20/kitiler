package dev.tronto.kitiler.stat.outgoing.adaptor.multik

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.outgoing.adaptor.multik.NDArrayImageData
import dev.tronto.kitiler.stat.domain.BandStatistics
import dev.tronto.kitiler.stat.domain.Percentile
import dev.tronto.kitiler.stat.outgoing.port.spi.ImageDataStatistics
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.data.DataType
import org.jetbrains.kotlinx.multik.ndarray.operations.toDoubleArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toFloatArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toIntArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toLongArray
import kotlin.collections.List
import kotlin.collections.associateWith
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.first
import kotlin.collections.forEach
import kotlin.collections.getValue
import kotlin.collections.last
import kotlin.collections.map
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.collections.sorted
import kotlin.collections.sum
import kotlin.collections.sumOf
import kotlin.math.pow
import kotlin.math.sqrt

class NDArrayImageDataStatistics : ImageDataStatistics {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }
    }

    override fun supports(imageData: ImageData): Boolean = imageData is NDArrayImageData<*>

    override suspend fun statistics(imageData: ImageData, percentiles: List<Percentile>): List<BandStatistics> =
        logger.logTrace("do statistics") {
            if (imageData !is NDArrayImageData<*>) {
                throw IllegalStateException("not supported.")
            }

            val maskArray = imageData.mask.toIntArray()
            val validPixels = logger.logTrace("mask check") {
                maskArray.sum()
            }
            if (validPixels == 0) {
                // 유효한 값이 없을 경우.
                (0..<imageData.band).map {
                    BandStatistics(
                        BandIndex(it + 1),
                        0.0,
                        0.0,
                        0.0,
                        0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0,
                        0.0,
                        maskArray.size,
                        0,
                        percentiles.map { BandStatistics.PercentileResult(it, 0.0) }
                    )
                }
            } else {
                when (imageData.data.dtype) {
                    DataType.IntDataType -> {
                        val dataArray = (imageData.data as D3Array<Int>).toIntArray()
                        val bandSize = imageData.width * imageData.height
                        (0..<imageData.band).map { band ->
                            CoroutineScope(Dispatchers.Default).async {
                                val valueGroup = mutableMapOf<Int, Int>()
                                val offset = band * bandSize
                                for (i in 0..<bandSize) {
                                    if (maskArray[i] == 0) {
                                        continue
                                    }
                                    val value = dataArray[i + offset]
                                    valueGroup[value] = valueGroup[value]?.plus(1) ?: 1
                                }
                                val sortedKeys = valueGroup.keys.sorted()
                                val min: Double = sortedKeys.first().toDouble()
                                val max: Double = sortedKeys.last().toDouble()
                                val valueEntries = valueGroup.entries
                                val sum = valueEntries.sumOf { it.key.toLong() * it.value }.toDouble()

                                val count = validPixels
                                val mean = sum / count

                                var (minorityValue, minoritySize) = valueEntries.first()
                                valueGroup.forEach { (v, s) ->
                                    if (minoritySize > s) {
                                        minoritySize = s
                                        minorityValue = v
                                    }
                                }

                                var (majorityValue, majoritySize) = valueEntries.first()
                                valueGroup.forEach { (v, s) ->
                                    if (majoritySize < s) {
                                        majoritySize = s
                                        majorityValue = v
                                    }
                                }

                                val variance = valueEntries.sumOf { (mean - it.key).pow(2) * it.value } / count
                                val std = sqrt(variance)
                                val medianTarget = count / 2
                                var medianCount = 0L
                                val median = sortedKeys.first {
                                    medianCount += valueGroup.getValue(it)
                                    medianCount >= medianTarget
                                }.toDouble()
                                val unique = valueGroup.keys.size

                                val percentileMap = percentiles.associateWith {
                                    val target = count * it.value / 100
                                    var valueCount = 0L
                                    sortedKeys.first {
                                        valueCount += valueGroup.getValue(it)
                                        valueCount >= target
                                    }.toDouble()
                                }
                                BandStatistics(
                                    bandIndex = BandIndex(band + 1),
                                    min = min,
                                    max = max,
                                    mean = mean,
                                    count = count,
                                    sum = sum,
                                    std = std,
                                    median = median,
                                    majority = majorityValue.toDouble(),
                                    minority = minorityValue.toDouble(),
                                    unique = unique,
                                    validPercent = validPixels.toDouble() / (maskArray.size) * 100,
                                    maskedPixels = maskArray.size - validPixels,
                                    validPixels = validPixels,
                                    percentiles = percentileMap.map {
                                        BandStatistics.PercentileResult(
                                            it.key,
                                            it.value
                                        )
                                    }
                                )
                            }
                        }.awaitAll()
                    }

                    DataType.LongDataType -> {
                        val dataArray = (imageData.data as D3Array<Long>).toLongArray()
                        val bandSize = imageData.width * imageData.height
                        (0..<imageData.band).map { band ->
                            CoroutineScope(Dispatchers.Default).async {
                                val valueGroup = mutableMapOf<Long, Int>()
                                val offset = band * bandSize
                                for (i in 0..<bandSize) {
                                    if (maskArray[i] == 0) {
                                        continue
                                    }
                                    val value = dataArray[i + offset]
                                    valueGroup[value] = valueGroup[value]?.plus(1) ?: 1
                                }
                                val sortedKeys = valueGroup.keys.sorted()
                                val min: Double = sortedKeys.first().toDouble()
                                val max: Double = sortedKeys.last().toDouble()
                                val valueEntries = valueGroup.entries
                                val sum = valueEntries.sumOf { it.key * it.value }.toDouble()

                                val count = validPixels
                                val mean = sum / count

                                var (minorityValue, minoritySize) = valueEntries.first()
                                valueGroup.forEach { (v, s) ->
                                    if (minoritySize > s) {
                                        minoritySize = s
                                        minorityValue = v
                                    }
                                }

                                var (majorityValue, majoritySize) = valueEntries.first()
                                valueGroup.forEach { (v, s) ->
                                    if (majoritySize < s) {
                                        majoritySize = s
                                        majorityValue = v
                                    }
                                }

                                val variance = valueEntries.sumOf { (mean - it.key).pow(2) * it.value } / count
                                val std = sqrt(variance)
                                val medianTarget = count / 2
                                var medianCount = 0L
                                val median = sortedKeys.first {
                                    medianCount += valueGroup.getValue(it)
                                    medianCount >= medianTarget
                                }.toDouble()
                                val unique = valueGroup.keys.size

                                val percentileMap = percentiles.associateWith {
                                    val target = count * it.value / 100
                                    var valueCount = 0L
                                    sortedKeys.first {
                                        valueCount += valueGroup.getValue(it)
                                        valueCount >= target
                                    }.toDouble()
                                }
                                BandStatistics(
                                    bandIndex = BandIndex(band + 1),
                                    min = min,
                                    max = max,
                                    mean = mean,
                                    count = count,
                                    sum = sum,
                                    std = std,
                                    median = median,
                                    majority = majorityValue.toDouble(),
                                    minority = minorityValue.toDouble(),
                                    unique = unique,
                                    validPercent = validPixels.toDouble() / (maskArray.size) * 100,
                                    maskedPixels = maskArray.size - validPixels,
                                    validPixels = validPixels,
                                    percentiles = percentileMap.map {
                                        BandStatistics.PercentileResult(
                                            it.key,
                                            it.value
                                        )
                                    }
                                )
                            }
                        }.awaitAll()
                    }
                    DataType.FloatDataType -> {
                        val dataArray = (imageData.data as D3Array<Float>).toFloatArray()
                        val bandSize = imageData.width * imageData.height
                        (0..<imageData.band).map { band ->
                            CoroutineScope(Dispatchers.Default).async {
                                val valueGroup = mutableMapOf<Float, Int>()
                                val offset = band * bandSize
                                for (i in 0..<bandSize) {
                                    if (maskArray[i] == 0) {
                                        continue
                                    }
                                    val value = dataArray[i + offset]
                                    valueGroup[value] = valueGroup[value]?.plus(1) ?: 1
                                }
                                val sortedKeys = valueGroup.keys.sorted()
                                val min: Double = sortedKeys.first().toDouble()
                                val max: Double = sortedKeys.last().toDouble()
                                val valueEntries = valueGroup.entries
                                val sum = valueEntries.sumOf { it.key.toDouble() * it.value }

                                val count = validPixels
                                val mean = sum / count

                                var (minorityValue, minoritySize) = valueEntries.first()
                                valueGroup.forEach { (v, s) ->
                                    if (minoritySize > s) {
                                        minoritySize = s
                                        minorityValue = v
                                    }
                                }

                                var (majorityValue, majoritySize) = valueEntries.first()
                                valueGroup.forEach { (v, s) ->
                                    if (majoritySize < s) {
                                        majoritySize = s
                                        majorityValue = v
                                    }
                                }

                                val variance = valueEntries.sumOf { (mean - it.key).pow(2) * it.value } / count
                                val std = sqrt(variance)
                                val medianTarget = count / 2
                                var medianCount = 0L
                                val median = sortedKeys.first {
                                    medianCount += valueGroup.getValue(it)
                                    medianCount >= medianTarget
                                }.toDouble()
                                val unique = valueGroup.keys.size

                                val percentileMap = percentiles.associateWith {
                                    val target = count * it.value / 100
                                    var valueCount = 0L
                                    sortedKeys.first {
                                        valueCount += valueGroup.getValue(it)
                                        valueCount >= target
                                    }.toDouble()
                                }
                                BandStatistics(
                                    bandIndex = BandIndex(band + 1),
                                    min = min,
                                    max = max,
                                    mean = mean,
                                    count = count,
                                    sum = sum,
                                    std = std,
                                    median = median,
                                    majority = majorityValue.toDouble(),
                                    minority = minorityValue.toDouble(),
                                    unique = unique,
                                    validPercent = validPixels.toDouble() / (maskArray.size) * 100,
                                    maskedPixels = maskArray.size - validPixels,
                                    validPixels = validPixels,
                                    percentiles = percentileMap.map {
                                        BandStatistics.PercentileResult(
                                            it.key,
                                            it.value
                                        )
                                    }
                                )
                            }
                        }.awaitAll()
                    }
                    DataType.DoubleDataType -> {
                        val dataArray = (imageData.data as D3Array<Double>).toDoubleArray()
                        val bandSize = imageData.width * imageData.height
                        (0..<imageData.band).map { band ->
                            CoroutineScope(Dispatchers.Default).async {
                                val valueGroup = mutableMapOf<Double, Int>()
                                val offset = band * bandSize
                                for (i in 0..<bandSize) {
                                    if (maskArray[i] == 0) {
                                        continue
                                    }
                                    val value = dataArray[i + offset]
                                    valueGroup[value] = valueGroup[value]?.plus(1) ?: 1
                                }
                                val sortedKeys = valueGroup.keys.sorted()
                                val min: Double = sortedKeys.first().toDouble()
                                val max: Double = sortedKeys.last().toDouble()
                                val valueEntries = valueGroup.entries
                                val sum = valueEntries.map { it.key * it.value }.sum()

                                val count = validPixels
                                val mean = sum / count

                                var (minorityValue, minoritySize) = valueEntries.first()
                                valueGroup.forEach { (v, s) ->
                                    if (minoritySize > s) {
                                        minoritySize = s
                                        minorityValue = v
                                    }
                                }

                                var (majorityValue, majoritySize) = valueEntries.first()
                                valueGroup.forEach { (v, s) ->
                                    if (majoritySize < s) {
                                        majoritySize = s
                                        majorityValue = v
                                    }
                                }

                                val variance = valueEntries.sumOf { (mean - it.key).pow(2) * it.value } / count
                                val std = sqrt(variance)
                                val medianTarget = count / 2
                                var medianCount = 0L
                                val median = sortedKeys.first {
                                    medianCount += valueGroup.getValue(it)
                                    medianCount >= medianTarget
                                }.toDouble()
                                val unique = valueGroup.keys.size

                                val percentileMap = percentiles.associateWith {
                                    val target = count * it.value / 100
                                    var valueCount = 0L
                                    sortedKeys.first {
                                        valueCount += valueGroup.getValue(it)
                                        valueCount >= target
                                    }.toDouble()
                                }
                                BandStatistics(
                                    bandIndex = BandIndex(band + 1),
                                    min = min,
                                    max = max,
                                    mean = mean,
                                    count = count,
                                    sum = sum,
                                    std = std,
                                    median = median,
                                    majority = majorityValue,
                                    minority = minorityValue,
                                    unique = unique,
                                    validPercent = validPixels.toDouble() / (maskArray.size) * 100,
                                    maskedPixels = maskArray.size - validPixels,
                                    validPixels = validPixels,
                                    percentiles = percentileMap.map {
                                        BandStatistics.PercentileResult(
                                            it.key,
                                            it.value
                                        )
                                    }
                                )
                            }
                        }.awaitAll()
                    }
                    else -> throw UnsupportedOperationException()
                }
            }
        }
}
