package dev.tronto.kitiler.core.incoming.controller.option

interface Option {
    companion object {
        operator fun <T : Option> T.plus(other: T): Iterable<T> = listOf(this, other)

        inline fun <reified T : Option> Iterable<Option>.getSingleOrNull(): T? =
            this.filterIsInstance<T>().firstOrNull()
    }
}
