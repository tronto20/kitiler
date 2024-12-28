package dev.tronto.kitiler.spring.application.swagger

import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.aot.hint.TypeReference
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider

class SwaggerRuntimeHints : RuntimeHintsRegistrar {
    private val classProvider =
        ClassPathScanningCandidateComponentProvider(false).apply {
            addIncludeFilter { _, _ -> true }
        }

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        classProvider.findCandidateComponents("io.swagger.v3.oas.models").forEach {
            hints.reflection().registerType(TypeReference.of(it.beanClassName!!), MemberCategory.INVOKE_PUBLIC_METHODS)
        }
        hints.resources().registerPattern("swagger/*")
    }
}
