package dev.tronto.kitiler.spring.autoconfigure.hints

import dev.tronto.kitiler.spring.autoconfigure.ApplicationContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.springframework.aot.hint.ExecutableMode
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import kotlin.reflect.full.companionObject

/**
 *  register [Serializable] reflection hints for aot
 */

class KotlinSerializableRuntimeHints : RuntimeHintsRegistrar {
    private val serializableProvider =
        ClassPathScanningCandidateComponentProvider(false).apply {
            addIncludeFilter(AnnotationTypeFilter(Serializable::class.java))
        }

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        val reflection = hints.reflection()
        val targetClasses = serializableProvider.findCandidateComponents(ApplicationContext.BASE_PACKAGE_NAME)
        targetClasses.forEach {
            val javaClass = Class.forName(it.beanClassName)
            val companionObject =
                javaClass.kotlin.companionObject?.java ?: kotlin.run {
                    println("$javaClass does not have companion object")
                    return@forEach
                }
            val companionObjectField =
                try {
                    // Find Default Companion Object
                    val defaultField = javaClass.getField("Companion")
                    if (defaultField.type != companionObject) {
                        throw NoSuchFieldException("Field 'Companion' is not Companion Object")
                    }
                    defaultField
                } catch (e: NoSuchFieldException) {
                    // Find Named Companion Object
                    javaClass.declaredFields.first { it.type == companionObject }
                }
            reflection.registerField(companionObjectField)
            val typeParameters = javaClass.typeParameters
            val method =
                companionObject.getMethod(
                    "serializer",
                    *Array(typeParameters.size) { KSerializer::class.java }
                )
            reflection.registerMethod(method, ExecutableMode.INVOKE)
        }
    }
}
