package dev.tronto.kitiler.spring.application.stat

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.stat.domain.BandStatistics
import dev.tronto.kitiler.stat.domain.Percentile
import dev.tronto.kitiler.stat.domain.Statistics
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation

val testStatistics = Statistics(
    listOf(
        BandStatistics(
            BandIndex(1),
            0.0,
            10.0,
            5.0,
            10,
            10.0,
            3.0,
            5.0,
            0.0,
            10.0,
            10,
            0.5,
            5,
            5,
            listOf(
                BandStatistics.PercentileResult(Percentile(2), 2.0)
            )
        )
    )
)

val statisticsFields = listOf(
    PayloadDocumentation.fieldWithPath("statistics.[].bandIndex").description("Band Index").type(JsonFieldType.NUMBER),
    PayloadDocumentation.fieldWithPath("statistics.[].min")
        .description("Minimum value of valid pixels").type(JsonFieldType.NUMBER),
    PayloadDocumentation.fieldWithPath("statistics.[].max")
        .description("Maximum value of valid pixels").type(JsonFieldType.NUMBER),
    PayloadDocumentation.fieldWithPath("statistics.[].mean")
        .description("Mean value of valid pixels").type(JsonFieldType.NUMBER),
    PayloadDocumentation.fieldWithPath("statistics.[].count")
        .description("Count of valid pixels").type(JsonFieldType.NUMBER),
    PayloadDocumentation.fieldWithPath("statistics.[].sum")
        .description("Sum of valid pixels").type(JsonFieldType.NUMBER),
    PayloadDocumentation.fieldWithPath("statistics.[].std")
        .description("Std of valid pixels").type(JsonFieldType.NUMBER),
    PayloadDocumentation.fieldWithPath("statistics.[].median")
        .description("Median of valid pixels").type(JsonFieldType.NUMBER),
    PayloadDocumentation.fieldWithPath("statistics.[].majority")
        .description("Majority of valid pixels").type(JsonFieldType.NUMBER),
    PayloadDocumentation.fieldWithPath("statistics.[].minority")
        .description("Minority of valid pixels").type(JsonFieldType.NUMBER),
    PayloadDocumentation.fieldWithPath("statistics.[].unique")
        .description("Count of Unique valid pixels").type(JsonFieldType.NUMBER),
    PayloadDocumentation.fieldWithPath("statistics.[].validPercent").description("validPixels / (validPixels + maskedPixels)").type(
        JsonFieldType.NUMBER
    ),
    PayloadDocumentation.fieldWithPath("statistics.[].maskedPixels")
        .description("Count of masked pixels.").type(JsonFieldType.NUMBER),
    PayloadDocumentation.fieldWithPath("statistics.[].validPixels")
        .description("Count of valid pixels").type(JsonFieldType.NUMBER),
    PayloadDocumentation.subsectionWithPath("statistics.[].percentiles")
        .description("Percentiles").type(JsonFieldType.ARRAY),
    PayloadDocumentation.fieldWithPath("statistics.[].percentiles.[].percentile")
        .description("Requested percentile").type(JsonFieldType.NUMBER),
    PayloadDocumentation.fieldWithPath("statistics.[].percentiles.[].value")
        .description("percentile value").type(JsonFieldType.NUMBER)
)
