package dev.tronto.kitiler.image.outgoing.adaptor.multik

import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.view
import org.jetbrains.kotlinx.multik.ndarray.operations.stack
import org.jetbrains.kotlinx.multik.ndarray.operations.toDoubleArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toFloatArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toIntArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toLongArray
import kotlin.random.Random
import kotlin.time.measureTime

class LinearRescaleTest :
    FunSpec({

        context("Int linearRescale") {
            val random = Random(0)
            val band = 3
            val width = 1024
            val height = 1024
            val data = IntArray(band * height * width) {
                random.nextInt(0, Short.MAX_VALUE.toInt())
            }
            val rangeFrom = 0..Short.MAX_VALUE.toInt()
            val rangeTo = 0..255

            test("linearRescale") {
                suspend fun doRescale() {
                    val testData = data.copyOf()
                    val ndarray = mk.ndarray(testData, band, height, width)
                    val rescaledData = (0..<ndarray.shape[0]).map { band ->
                        CoroutineScope(Dispatchers.Default).async {
                            linearRescale<Int, D2>(ndarray.view(band), NumberRange(rangeFrom), rangeTo)
                        }
                    }.awaitAll()

                    val result = mk.stack(rescaledData)
                }

                // 초기화 시간 제거
                doRescale()
                val resultTime = measureTime {
                    repeat(10) {
                        doRescale()
                    }
                }

                println(resultTime)
            }

            test("linearRescale2") {
                fun doRescale2() {
                    val testData = data.copyOf()
                    val ndarray = mk.ndarray(testData, band, height, width)
                    val array = ndarray.toIntArray()
                    val pixelSizePerBand = ndarray.shape[1] * ndarray.shape[2]
                    (0..<ndarray.shape[0]).forEach { band ->
                        linearRescaleToInt(array, band * pixelSizePerBand, pixelSizePerBand, rangeFrom, rangeTo)
                    }
                    val result = mk.ndarray(array, ndarray.shape[0], ndarray.shape[1], ndarray.shape[2])
                }

                // 초기화 시간 제거
                doRescale2()
                val resultTime = measureTime {
                    repeat(10) {
                        doRescale2()
                    }
                }

                println(resultTime)
            }
        }

        context("Long linearRescale") {
            val random = Random(0)
            val band = 3
            val width = 1024
            val height = 1024
            val data = LongArray(band * height * width) {
                random.nextLong(0, Long.MAX_VALUE)
            }
            val rangeFrom = 0..Long.MAX_VALUE
            val rangeTo = 0..255

            test("linearRescale") {
                suspend fun doRescale() {
                    val testData = data.copyOf()
                    val ndarray = mk.ndarray(testData, band, height, width)
                    val rescaledData = (0..<ndarray.shape[0]).map { band ->
                        CoroutineScope(Dispatchers.Default).async {
                            linearRescale<Long, D2>(ndarray.view(band), NumberRange(rangeFrom), rangeTo)
                        }
                    }.awaitAll()

                    val result = mk.stack(rescaledData)
                }

                // 초기화 시간 제거
                doRescale()
                val resultTime = measureTime {
                    repeat(10) {
                        doRescale()
                    }
                }

                println(resultTime)
            }

            test("linearRescale2") {
                fun doRescale2() {
                    val testData = data.copyOf()
                    val ndarray = mk.ndarray(testData, band, height, width)
                    val array = ndarray.toLongArray()
                    val targetArray = IntArray(array.size)
                    val pixelSizePerBand = ndarray.shape[1] * ndarray.shape[2]
                    (0..<ndarray.shape[0]).forEach { band ->
                        linearRescaleToInt(array, band * pixelSizePerBand, pixelSizePerBand, rangeFrom, rangeTo, targetArray)
                    }
                    val result = mk.ndarray(targetArray, ndarray.shape[0], ndarray.shape[1], ndarray.shape[2])
                }

                // 초기화 시간 제거
                doRescale2()
                val resultTime = measureTime {
                    repeat(10) {
                        doRescale2()
                    }
                }

                println(resultTime)
            }
        }

        context("Float linearRescale") {
            val random = Random(0)
            val band = 3
            val width = 1024
            val height = 1024
            val data = FloatArray(band * height * width) {
                random.nextFloat()
            }
            val rangeFrom = 0f..Float.MAX_VALUE
            val rangeTo = 0..255

            test("linearRescale") {
                suspend fun doRescale() {
                    val testData = data.copyOf()
                    val ndarray = mk.ndarray(testData, band, height, width)
                    val rescaledData = (0..<ndarray.shape[0]).map { band ->
                        CoroutineScope(Dispatchers.Default).async {
                            linearRescale<Float, D2>(ndarray.view(band), NumberRange(rangeFrom), rangeTo)
                        }
                    }.awaitAll()

                    val result = mk.stack(rescaledData)
                }

                // 초기화 시간 제거
                doRescale()
                val resultTime = measureTime {
                    repeat(10) {
                        doRescale()
                    }
                }

                println(resultTime)
            }

            test("linearRescale2") {
                fun doRescale2() {
                    val testData = data.copyOf()
                    val ndarray = mk.ndarray(testData, band, height, width)
                    val array = ndarray.toFloatArray()
                    val targetArray = IntArray(array.size)
                    val pixelSizePerBand = ndarray.shape[1] * ndarray.shape[2]
                    (0..<ndarray.shape[0]).forEach { band ->
                        linearRescaleToInt(array, band * pixelSizePerBand, pixelSizePerBand, rangeFrom, rangeTo, targetArray)
                    }
                    val result = mk.ndarray(targetArray, ndarray.shape[0], ndarray.shape[1], ndarray.shape[2])
                }

                // 초기화 시간 제거
                doRescale2()
                val resultTime = measureTime {
                    repeat(10) {
                        doRescale2()
                    }
                }

                println(resultTime)
            }
        }

        context("Double linearRescale") {
            val random = Random(0)
            val band = 3
            val width = 1024
            val height = 1024
            val data = DoubleArray(band * height * width) {
                random.nextDouble(0.0, Double.MAX_VALUE)
            }
            val rangeFrom = 0.0..Int.MAX_VALUE.toDouble()
            val rangeTo = 0..255

            test("linearRescale") {
                suspend fun doRescale() {
                    val testData = data.copyOf()
                    val ndarray = mk.ndarray(testData, band, height, width)
                    val rescaledData = (0..<ndarray.shape[0]).map { band ->
                        CoroutineScope(Dispatchers.Default).async {
                            linearRescale<Double, D2>(ndarray.view(band), NumberRange(rangeFrom), rangeTo)
                        }
                    }.awaitAll()

                    val result = mk.stack(rescaledData)
                }

                // 초기화 시간 제거
                doRescale()
                val resultTime = measureTime {
                    repeat(10) {
                        doRescale()
                    }
                }

                println(resultTime)
            }

            test("linearRescale2") {
                fun doRescale2() {
                    val testData = data.copyOf()
                    val ndarray = mk.ndarray(testData, band, height, width)
                    val array = ndarray.toDoubleArray()
                    val targetArray = IntArray(array.size)
                    val pixelSizePerBand = ndarray.shape[1] * ndarray.shape[2]
                    (0..<ndarray.shape[0]).forEach { band ->
                        linearRescaleToInt(array, band * pixelSizePerBand, pixelSizePerBand, rangeFrom, rangeTo, targetArray)
                    }
                    val result = mk.ndarray(targetArray, ndarray.shape[0], ndarray.shape[1], ndarray.shape[2])
                }

                // 초기화 시간 제거
                doRescale2()
                val resultTime = measureTime {
                    repeat(10) {
                        doRescale2()
                    }
                }

                println(resultTime)
            }
        }
    })
