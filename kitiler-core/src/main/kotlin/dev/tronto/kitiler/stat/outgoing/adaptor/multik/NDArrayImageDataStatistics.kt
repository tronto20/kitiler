package dev.tronto.kitiler.stat.outgoing.adaptor.multik

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.core.utils.ResourceManagerContext
import dev.tronto.kitiler.core.utils.logTrace
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.stat.domain.BandStatistics
import dev.tronto.kitiler.stat.domain.Percentile
import dev.tronto.kitiler.stat.outgoing.port.spi.ImageDataStatistics
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlin.math.pow
import kotlin.math.sqrt

class NDArrayImageDataStatistics : ImageDataStatistics {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }
    }

    override fun supports(imageData: ImageData): Boolean = true

    override suspend fun statistics(imageData: ImageData, percentiles: List<Percentile>): List<BandStatistics> =
        logger.logTrace("do statistics") {
            val validArray = imageData.getValidArray()
            val validPixels = logger.logTrace("mask check") {
                validArray?.count {
                    it
                } ?: (imageData.width * imageData.height)
            }
            if (validPixels == 0) {
                // 유효한 값이 없을 경우.
                logger.warn { "No valid pixels found" }
                (0..<imageData.bandCount).map {
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
                        imageData.width * imageData.height,
                        0,
                        percentiles.map { BandStatistics.PercentileResult(it, 0.0) }
                    )
                }
            } else {
                val data = imageData.getBandBuffer()
                val bandSize = imageData.width * imageData.height
                val deferred = when {
                    data.isIntArray -> {
                        val dataArray = data.intArray
                        (0..<imageData.bandCount).map { band ->
                            CoroutineScope(Dispatchers.Unconfined + ResourceManagerContext()).async {
                                val valueGroup = mutableMapOf<Int, Int>()
                                val offset = band * bandSize
                                if (validArray == null) {
                                    for (i in 0..<bandSize) {
                                        val value = dataArray[i + offset]
                                        valueGroup[value] = valueGroup[value]?.plus(1) ?: 1
                                    }
                                } else {
                                    for (i in 0..<bandSize) {
                                        if (validArray[i]) {
                                            val value = dataArray[i + offset]
                                            valueGroup[value] = valueGroup[value]?.plus(1) ?: 1
                                        }
                                    }
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
                                    validPercent =
                                    validArray?.let { validPixels.toDouble() / (validArray.size) * 100 } ?: 100.0,
                                    maskedPixels = validArray?.let { validArray.size - validPixels } ?: 0,
                                    validPixels = validPixels,
                                    percentiles = percentileMap.map {
                                        BandStatistics.PercentileResult(
                                            it.key,
                                            it.value
                                        )
                                    }
                                )
                            }
                        }
                    }

                    data.isLongArray -> {
                        val dataArray = data.longArray
                        (0..<imageData.bandCount).map { band ->
                            CoroutineScope(Dispatchers.Unconfined + ResourceManagerContext()).async {
                                val valueGroup = mutableMapOf<Long, Int>()
                                val offset = band * bandSize
                                if (validArray == null) {
                                    for (i in 0..<bandSize) {
                                        val value = dataArray[i + offset]
                                        valueGroup[value] = valueGroup[value]?.plus(1) ?: 1
                                    }
                                } else {
                                    for (i in 0..<bandSize) {
                                        if (!validArray[i]) {
                                            continue
                                        }
                                        val value = dataArray[i + offset]
                                        valueGroup[value] = valueGroup[value]?.plus(1) ?: 1
                                    }
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
                                    validPercent =
                                    validArray?.let { validPixels.toDouble() / (validArray.size) * 100 } ?: 100.0,
                                    maskedPixels = validArray?.let { validArray.size - validPixels } ?: 0,
                                    validPixels = validPixels,
                                    percentiles = percentileMap.map {
                                        BandStatistics.PercentileResult(
                                            it.key,
                                            it.value
                                        )
                                    }
                                )
                            }
                        }
                    }

                    data.isFloatArray -> {
                        val dataArray = data.floatArray
                        (0..<imageData.bandCount).map { band ->
                            CoroutineScope(Dispatchers.Unconfined + ResourceManagerContext()).async {
                                val valueGroup = mutableMapOf<Float, Int>()
                                val offset = band * bandSize
                                if (validArray == null) {
                                    for (i in 0..<bandSize) {
                                        val value = dataArray[i + offset]
                                        valueGroup[value] = valueGroup[value]?.plus(1) ?: 1
                                    }
                                } else {
                                    for (i in 0..<bandSize) {
                                        if (!validArray[i]) {
                                            continue
                                        }
                                        val value = dataArray[i + offset]
                                        valueGroup[value] = valueGroup[value]?.plus(1) ?: 1
                                    }
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
                                    validPercent =
                                    validArray?.let { validPixels.toDouble() / (validArray.size) * 100 } ?: 100.0,
                                    maskedPixels = validArray?.let { validArray.size - validPixels } ?: 0,
                                    validPixels = validPixels,
                                    percentiles = percentileMap.map {
                                        BandStatistics.PercentileResult(
                                            it.key,
                                            it.value
                                        )
                                    }
                                )
                            }
                        }
                    }

                    data.isDoubleArray -> {
                        val dataArray = data.doubleArray
                        (0..<imageData.bandCount).map { band ->
                            CoroutineScope(Dispatchers.Unconfined + ResourceManagerContext()).async {
                                val valueGroup = mutableMapOf<Double, Int>()
                                val offset = band * bandSize
                                if (validArray == null) {
                                    for (i in 0..<bandSize) {
                                        val value = dataArray[i + offset]
                                        valueGroup[value] = valueGroup[value]?.plus(1) ?: 1
                                    }
                                } else {
                                    for (i in 0..<bandSize) {
                                        if (!validArray[i]) {
                                            continue
                                        }
                                        val value = dataArray[i + offset]
                                        valueGroup[value] = valueGroup[value]?.plus(1) ?: 1
                                    }
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
                                    validPercent =
                                    validArray?.let { validPixels.toDouble() / (validArray.size) * 100 } ?: 100.0,
                                    maskedPixels = validArray?.let { validArray.size - validPixels } ?: 0,
                                    validPixels = validPixels,
                                    percentiles = percentileMap.map {
                                        BandStatistics.PercentileResult(
                                            it.key,
                                            it.value
                                        )
                                    }
                                )
                            }
                        }
                    }

                    else -> throw UnsupportedOperationException()
                }
                deferred.awaitAll()
            }
        }
}
