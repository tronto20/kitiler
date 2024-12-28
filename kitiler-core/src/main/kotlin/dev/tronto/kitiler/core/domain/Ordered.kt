package dev.tronto.kitiler.core.domain

import org.springframework.core.Ordered

interface Ordered : Ordered {
    override fun getOrder(): Int = 0
}
