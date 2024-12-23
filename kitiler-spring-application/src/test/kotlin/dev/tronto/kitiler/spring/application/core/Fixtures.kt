package dev.tronto.kitiler.spring.application.core

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.core.domain.BandInfo
import dev.tronto.kitiler.core.domain.Bounds
import dev.tronto.kitiler.core.domain.ColorInterpretation
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.domain.Info
import dev.tronto.kitiler.spring.application.enumValues
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.snippet.Attributes

val testBounds = Bounds(doubleArrayOf(0.0, 1.0, 1.0, 2.0))
val boundsFields = listOf(
    PayloadDocumentation.fieldWithPath("bounds").type(JsonFieldType.ARRAY)
        .description("bounds")
        .attributes(Attributes.Attribute("itemsType", JsonFieldType.NUMBER))
)
val testInfo = Info(
    "name",
    doubleArrayOf(0.0, 1.0, 2.0, 3.0),
    DataType.UInt8,
    "GTiff",
    3000,
    3000,
    "NoData",
    0.0,
    3,
    listOf(
        BandInfo(
            BandIndex(1),
            DataType.UInt8,
            colorInterpolation = ColorInterpretation.RedBand
        ),
        BandInfo(
            BandIndex(2),
            DataType.UInt8,
            description = "description",
            colorInterpolation = ColorInterpretation.GreenBand
        ),
        BandInfo(
            BandIndex(3),
            DataType.UInt8,
            colorInterpolation = ColorInterpretation.BlueBand,
            metadata = mapOf("metadata" to "1")
        )
    )
)

val infoFields = boundsFields + listOf(
    PayloadDocumentation.fieldWithPath("name").type(JsonFieldType.STRING).description("name"),
    PayloadDocumentation.fieldWithPath("driver").type(JsonFieldType.STRING).description("driver"),
    PayloadDocumentation.fieldWithPath("dataType").type(JsonFieldType.STRING).description("dataType")
        .enumValues(DataType.entries),
    PayloadDocumentation.fieldWithPath("width").type(JsonFieldType.NUMBER).description("width"),
    PayloadDocumentation.fieldWithPath("height").type(JsonFieldType.NUMBER).description("height"),
    PayloadDocumentation.fieldWithPath("nodataType").type(JsonFieldType.STRING).description("noData Type"),
    PayloadDocumentation.fieldWithPath("nodataValue").type(JsonFieldType.NUMBER).description("noData Value"),
    PayloadDocumentation.fieldWithPath("bandCount").type(JsonFieldType.NUMBER).description("band count"),
    PayloadDocumentation.subsectionWithPath("bandInfo").type(JsonFieldType.ARRAY).description("band info"),
    PayloadDocumentation.fieldWithPath("bandInfo.[].dataType").type(JsonFieldType.STRING).description("band dataType")
        .enumValues(DataType.entries),
    PayloadDocumentation.fieldWithPath("bandInfo.[].bandIndex").type(JsonFieldType.NUMBER).description("band index"),
    PayloadDocumentation.fieldWithPath("bandInfo.[].colorInterpolation").type(JsonFieldType.STRING).description("band color interpolation")
        .enumValues(ColorInterpretation.entries).optional(),
    PayloadDocumentation.fieldWithPath("bandInfo.[].description").type(JsonFieldType.STRING).description("band description").optional(),
    PayloadDocumentation.subsectionWithPath("bandInfo.[].metadata").type(JsonFieldType.OBJECT).description("band metadata").optional()
)
