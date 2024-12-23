package dev.tronto.kitiler.image.outgoing.adaptor.multik

import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.D3
import org.jetbrains.kotlinx.multik.ndarray.data.D4
import org.jetbrains.kotlinx.multik.ndarray.data.Dimension
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.data.dimensionOf

/**
 *  squeeze 또는 asNDArray 같은 함수들이 D1, D2 대신 DN 을 사용하여 일부 로직 (e.g. cat)에서 의도한 대로 작업되지 않고 있음.
 *  이를 방지하기 위해 dimension 을 맞춰주는 함수
 */
fun <T, D : Dimension> NDArray<T, D>.normalize(): NDArray<T, D> = when (this.dim) {
    D1, D2, D3, D4 -> this
    else -> {
        val dimension = dimensionOf<D>(dim.d)
        NDArray(data, offset, shape, strides, dimension, base ?: this)
    }
}
