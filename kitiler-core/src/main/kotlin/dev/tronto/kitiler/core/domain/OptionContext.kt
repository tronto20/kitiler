package dev.tronto.kitiler.core.domain

import dev.tronto.kitiler.core.incoming.controller.option.ArgumentType
import dev.tronto.kitiler.core.incoming.controller.option.Option
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider

interface OptionContext {
    fun <T : Option> getOptionProviderOrNull(argumentType: ArgumentType<T>): OptionProvider<T>?
    fun <T : Option> hasOptionProvider(argumentType: ArgumentType<T>): Boolean =
        getOptionProviderOrNull(argumentType) != null

    fun <T : Option> getOptionProvider(argumentType: ArgumentType<T>): OptionProvider<T> =
        getOptionProviderOrNull(argumentType)
            ?: throw IllegalStateException("OptionProvider of $argumentType is not provided.")

    fun getAllOptionProviders(): Collection<OptionProvider<*>>

    fun put(vararg options: OptionProvider<*>)

    companion object {
        class SimpleWrap(private val optionProviderMap: MutableMap<ArgumentType<*>, OptionProvider<*>>) :
            OptionContext {
            override fun <T : Option> getOptionProviderOrNull(argumentType: ArgumentType<T>): OptionProvider<T>? =
                optionProviderMap[argumentType] as OptionProvider<T>?

            override fun getAllOptionProviders(): Collection<OptionProvider<*>> = optionProviderMap.values

            override fun put(vararg options: OptionProvider<*>) {
                options.forEach {
                    optionProviderMap.put(it.argumentType, it)
                }
            }
        }

        fun wrap(vararg optionProvider: OptionProvider<*>) = SimpleWrap(
            optionProvider.associateBy { it.argumentType }.toMutableMap()
        )
    }
}
