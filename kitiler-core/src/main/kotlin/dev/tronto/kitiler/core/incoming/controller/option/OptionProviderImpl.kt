package dev.tronto.kitiler.core.incoming.controller.option

class OptionProviderImpl<O : Option>(
    override val argumentType: ArgumentType<O>,
    private val parserMap: Map<ArgumentType<out O>, List<OptionParser<out O>>>,
    private val parseCache: Map<ArgumentType<out O>, O?> = mapOf(),
) : OptionProvider<O> {
    companion object {
        suspend fun create(request: Request, parsers: List<OptionParser<out Option>>): OptionProviderImpl<Option> {
            val parserMap = parsers.groupBy { it.type }
            return create(request, parserMap)
        }

        suspend fun create(
            request: Request,
            parserMap: Map<ArgumentType<out Option>, List<OptionParser<out Option>>>,
        ): OptionProviderImpl<Option> {
            val parseValues = parserMap.mapValues {
                it.value.asReversed().firstNotNullOfOrNull { it.parse(request) }
            }
            return OptionProviderImpl<Option>(
                ArgumentType<Option>(),
                parserMap,
                parseValues
            )
        }
    }

    override fun <T : O> filter(argumentType: ArgumentType<T>): OptionProvider<T> = OptionProviderImpl<T>(
        argumentType,
        parserMap
            .filter { it.key.isSubtypeOf(argumentType) }
            .mapKeys {
                @Suppress("UNCHECKED_CAST")
                it.key as ArgumentType<out T>
            }
            .mapValues {
                @Suppress("UNCHECKED_CAST")
                it.value as List<OptionParser<out T>>
            },
        mutableMapOf<ArgumentType<out T>, T?>().apply {
            putAll(
                parseCache.filter { it.key.isSubtypeOf(argumentType) }
                    .mapKeys {
                        @Suppress("UNCHECKED_CAST")
                        it.key as ArgumentType<out T>
                    }
                    .mapValues {
                        @Suppress("UNCHECKED_CAST")
                        it.value as T?
                    }
            )
        }
    )

    override fun <T : O> filterNot(argumentType: ArgumentType<T>): OptionProvider<O> = OptionProviderImpl<O>(
        this.argumentType,
        parserMap.filterNot { it.key.isSubtypeOf(argumentType) },
        parseCache.filterNot { it.key.isSubtypeOf(argumentType) }.toMutableMap()
    )

    override fun <T : O> getAll(argumentType: ArgumentType<T>): List<T> {
        val cachedType = mutableSetOf<ArgumentType<out T>>()
        return parseCache.entries
            .filter { it.key.isSubtypeOf(argumentType) }
            .onEach {
                @Suppress("UNCHECKED_CAST")
                cachedType.add(it.key as ArgumentType<out T>)
            }
            .mapNotNull {
                @Suppress("UNCHECKED_CAST")
                it.value as T?
            }
    }

    override fun <T : O> get(argumentType: ArgumentType<T>): T {
        getOrNull(argumentType)?.let { return it }
        val parser = parserMap[argumentType]?.firstOrNull()
            ?: throw IllegalStateException("Parameter parser for $argumentType not defined.")
        throw parser.generateMissingException()
    }

    override fun <T : O> getOrNull(argumentType: ArgumentType<T>): T? = if (parseCache.containsKey(argumentType)) {
        @Suppress("UNCHECKED_CAST")
        parseCache[argumentType] as T?
    } else {
        null
    }

    override fun <T : O> plus(option: T, argumentType: ArgumentType<T>): OptionProvider<O> = OptionProviderImpl<O>(
        this.argumentType,
        parserMap,
        mutableMapOf<ArgumentType<out O>, O?>().apply {
            putAll(parseCache)
            put(argumentType, option)
        }
    )

    override fun plus(other: OptionProvider<O>): OptionProvider<O> =
        CombinedOptionProvider(listOf(this, other), argumentType)

    override fun <T : O> boxAll(argumentType: ArgumentType<T>): Map<String, List<String>> {
        val targetParsers = parserMap.filter {
            it.key.isSubtypeOf(argumentType)
        }.mapKeys {
            @Suppress("UNCHECKED_CAST")
            it.key as ArgumentType<T>
        }.mapValues {
            @Suppress("UNCHECKED_CAST")
            it.value as List<OptionParser<out T>>
        }

        val resultMap = mutableMapOf<String, List<String>>()
        targetParsers.entries.forEach { (type, parsers) ->
            val value = getOrNull(type) ?: return@forEach
            val boxParser = parsers.firstOrNull()
                ?: throw IllegalStateException("Parameter parser for $argumentType not defined.")

            @Suppress("UNCHECKED_CAST")
            val box = (boxParser as OptionParser<T>).box(value)
            resultMap.putAll(box)
        }
        return resultMap
    }
}
