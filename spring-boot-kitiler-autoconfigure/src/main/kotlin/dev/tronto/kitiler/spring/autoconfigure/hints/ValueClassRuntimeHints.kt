package dev.tronto.kitiler.spring.autoconfigure.hints

import dev.tronto.kitiler.spring.autoconfigure.ApplicationContext
import org.springframework.aot.hint.ExecutableMode
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter

class ValueClassRuntimeHints : RuntimeHintsRegistrar {
    private val provider =
        ClassPathScanningCandidateComponentProvider(false).apply {
            addIncludeFilter(AnnotationTypeFilter(JvmInline::class.java))
        }

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        val reflection = hints.reflection()
        provider.findCandidateComponents(ApplicationContext.BASE_PACKAGE_NAME).map {
            Class.forName(it.beanClassName)
        }.filter {
            it.kotlin.isValue
        }.flatMap {
            it.methods.filter { it.name == "box-impl" || it.name == "unbox-impl" }
        }.forEach {
            reflection.registerMethod(it, ExecutableMode.INVOKE)
        }
    }
}
