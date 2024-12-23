package dev.tronto.kitiler.core.utils

class TemplateString(val value: String) {
    companion object {
        val regex = Regex("\\{(.+?)\\}")
    }

    val variables: List<String> = regex.findAll(value).map {
        it.value
            .trimStart('{')
            .trimEnd('}')
            .lowercase()
    }.toList()

    fun putAll(variables: Map<String, List<String>>): String {
        return this.variables.fold(value) { acc, it ->
            val variable = variables[it]?.firstOrNull() ?: return@fold acc
            acc.replace("{$it}", variable, true)
        }
    }
}
