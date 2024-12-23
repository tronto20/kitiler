package dev.tronto.kitiler.core.incoming.controller.option

interface Request {
    companion object Empty : Request {
        override fun parameter(key: String): List<String> = emptyList()

        override fun option(key: String): List<String> = emptyList()
    }
    fun parameter(key: String): List<String>

    fun option(key: String): List<String>
}
