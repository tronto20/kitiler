package dev.tronto.kitiler.core.domain

enum class ResamplingAlgorithm {
    NEAREST,
    BILINEAR,
    CUBIC,
    CUBIC_SPLINE,
    LANCZOS,
    AVERAGE,
    RMS,
    MODE,
    ;

    companion object {
        private val entryMap: Map<String, ResamplingAlgorithm> =
            ResamplingAlgorithm.entries.associateBy {
                it.name.lowercase()
            }

        operator fun invoke(name: String): ResamplingAlgorithm =
            entryMap[name.lowercase()] ?: throw IllegalArgumentException("Unknown resampling algorithm: $name")
    }
}
