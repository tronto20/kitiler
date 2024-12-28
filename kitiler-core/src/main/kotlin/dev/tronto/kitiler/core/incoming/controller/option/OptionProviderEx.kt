package dev.tronto.kitiler.core.incoming.controller.option

inline fun <O : Option, reified T : O> OptionProvider<O>.filter(): OptionProvider<T> = filter(ArgumentType<T>())
inline fun <O : Option, reified T : O> OptionProvider<O>.filterNot(): OptionProvider<O> = filterNot(ArgumentType<T>())

inline fun <O : Option, reified T : O> OptionProvider<O>.getAll(): List<T> = getAll(ArgumentType<T>())
inline fun <O : Option, reified T : O> OptionProvider<O>.get(): T = get(ArgumentType<T>())
inline fun <O : Option, reified T : O> OptionProvider<O>.getOrNull(): T? = getOrNull(ArgumentType<T>())
inline fun <O : Option, reified T : O> OptionProvider<O>.contains(): Boolean = contains(ArgumentType<T>())

inline operator fun <O : Option, reified T : O> OptionProvider<O>.plus(option: T): OptionProvider<O> =
    plus(option, ArgumentType<T>())

inline fun <O : Option, reified T : O> OptionProvider<O>.boxAll(): Map<String, List<String>> = boxAll(ArgumentType<T>())
