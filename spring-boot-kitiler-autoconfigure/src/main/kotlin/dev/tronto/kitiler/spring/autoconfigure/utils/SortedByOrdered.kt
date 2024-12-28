package dev.tronto.kitiler.spring.autoconfigure.utils

import dev.tronto.kitiler.core.domain.Ordered

/***
 *  Spring 의 Ordered 와 core 의 Ordered 포함 정렬
 */
fun <T : Any> Iterable<T>.sortedByOrdered() = sortedBy {
    when (it) {
        is Ordered -> it.order
        is org.springframework.core.Ordered -> it.order
        else -> 0
    }
}
