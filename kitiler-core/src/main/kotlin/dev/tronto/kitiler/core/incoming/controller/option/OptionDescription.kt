package dev.tronto.kitiler.core.incoming.controller.option

data class OptionDescription<T : Any>(
    val type: ArgumentType<T>,
    val name: String,
    val description: String,
    val sample: T,
    val enums: Iterable<T>? = null,
    val default: T? = null,
)

inline fun <reified T : Any> OptionDescription(
    name: String,
    description: String,
    sample: T,
    enums: Iterable<T>? = null,
    default: T? = null,
): OptionDescription<T> = OptionDescription(
    ArgumentType<T>(),
    name,
    description,
    sample,
    enums,
    default
)
