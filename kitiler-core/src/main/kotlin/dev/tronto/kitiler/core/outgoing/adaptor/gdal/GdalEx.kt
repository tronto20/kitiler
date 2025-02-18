package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.ColorInterpretation
import dev.tronto.kitiler.core.domain.DataType
import dev.tronto.kitiler.core.domain.DataType.CFloat32
import dev.tronto.kitiler.core.domain.DataType.CFloat64
import dev.tronto.kitiler.core.domain.DataType.CInt16
import dev.tronto.kitiler.core.domain.DataType.CInt32
import dev.tronto.kitiler.core.domain.DataType.Float32
import dev.tronto.kitiler.core.domain.DataType.Float64
import dev.tronto.kitiler.core.domain.DataType.Int16
import dev.tronto.kitiler.core.domain.DataType.Int32
import dev.tronto.kitiler.core.domain.DataType.Int64
import dev.tronto.kitiler.core.domain.DataType.Int8
import dev.tronto.kitiler.core.domain.DataType.UInt16
import dev.tronto.kitiler.core.domain.DataType.UInt32
import dev.tronto.kitiler.core.domain.DataType.UInt64
import dev.tronto.kitiler.core.domain.DataType.UInt8
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gdal.gdal.MajorObject
import org.gdal.gdal.WarpOptions
import org.gdal.gdal.gdal
import org.gdal.gdalconst.gdalconst

@PublishedApi
internal object GdalEx {
    @JvmStatic
    val logger = KotlinLogging.logger { }

    @JvmStatic
    val errorCodes = listOf(gdalconst.CE_Failure, gdalconst.CE_Fatal)
}

inline fun <T> WarpOptions.use(block: (WarpOptions) -> T): T = try {
    block(this)
} finally {
    try {
        this.delete()
    } catch (e: RuntimeException) {
        // ignore
        GdalEx.logger.warn(e) { "Failed to delete ${this::class.simpleName}." }
    }
}

inline fun <O : MajorObject, T> O.use(block: (O) -> T): T = try {
    block(this)
} finally {
    try {
        this.delete()
    } catch (e: RuntimeException) {
        // ignore
        GdalEx.logger.warn(e) { "Failed to delete ${this::class.simpleName}." }
    }
}

inline fun <O : MajorObject> O.handleError(block: O.() -> Int) {
    val result = block()
    if (result in GdalEx.errorCodes) {
        throw IllegalStateException(gdal.GetLastErrorMsg())
    }
}

val DataType.gdalConst: Int
    get() = when (this) {
        Int8 -> org.gdal.gdalconst.gdalconst.GDT_Int8
        UInt8 -> org.gdal.gdalconst.gdalconst.GDT_Byte
        UInt16 -> org.gdal.gdalconst.gdalconst.GDT_UInt16
        Int16 -> org.gdal.gdalconst.gdalconst.GDT_Int16
        UInt32 -> org.gdal.gdalconst.gdalconst.GDT_UInt32
        Int32 -> org.gdal.gdalconst.gdalconst.GDT_Int32
        UInt64 -> org.gdal.gdalconst.gdalconst.GDT_UInt64
        Int64 -> org.gdal.gdalconst.gdalconst.GDT_Int64
        Float32 -> org.gdal.gdalconst.gdalconst.GDT_Float32
        Float64 -> org.gdal.gdalconst.gdalconst.GDT_Float64
        CInt16 -> org.gdal.gdalconst.gdalconst.GDT_CInt16
        CInt32 -> org.gdal.gdalconst.gdalconst.GDT_CInt32
        CFloat32 -> org.gdal.gdalconst.gdalconst.GDT_CFloat32
        CFloat64 -> org.gdal.gdalconst.gdalconst.GDT_CFloat64
        else -> throw UnsupportedOperationException()
    }

operator fun DataType.Companion.get(gdalConst: Int): DataType = when (gdalConst) {
    org.gdal.gdalconst.gdalconst.GDT_Int8 -> DataType.Int8
    org.gdal.gdalconst.gdalconst.GDT_Byte -> DataType.UInt8
    org.gdal.gdalconst.gdalconst.GDT_UInt16 -> DataType.UInt16
    org.gdal.gdalconst.gdalconst.GDT_Int16 -> DataType.Int16
    org.gdal.gdalconst.gdalconst.GDT_UInt32 -> DataType.UInt32
    org.gdal.gdalconst.gdalconst.GDT_Int32 -> DataType.Int32
    org.gdal.gdalconst.gdalconst.GDT_UInt64 -> DataType.UInt64
    org.gdal.gdalconst.gdalconst.GDT_Int64 -> DataType.Int64
    org.gdal.gdalconst.gdalconst.GDT_Float32 -> DataType.Float32
    org.gdal.gdalconst.gdalconst.GDT_Float64 -> DataType.Float64
    org.gdal.gdalconst.gdalconst.GDT_CInt16 -> DataType.CInt16
    org.gdal.gdalconst.gdalconst.GDT_CInt32 -> DataType.CInt32
    org.gdal.gdalconst.gdalconst.GDT_CFloat32 -> DataType.CFloat32
    org.gdal.gdalconst.gdalconst.GDT_CFloat64 -> DataType.CFloat64
    else -> throw IllegalArgumentException("Invalid gdalConst for DataType: $gdalConst")
}

val ColorInterpretation.gdalConst: Int
    get() = when (this) {
        ColorInterpretation.Undefined -> org.gdal.gdalconst.gdalconst.GCI_Undefined
        ColorInterpretation.GrayIndex -> org.gdal.gdalconst.gdalconst.GCI_GrayIndex
        ColorInterpretation.PaletteIndex -> org.gdal.gdalconst.gdalconst.GCI_PaletteIndex
        ColorInterpretation.Red -> org.gdal.gdalconst.gdalconst.GCI_RedBand
        ColorInterpretation.Green -> org.gdal.gdalconst.gdalconst.GCI_GreenBand
        ColorInterpretation.Blue -> org.gdal.gdalconst.gdalconst.GCI_BlueBand
        ColorInterpretation.Alpha -> org.gdal.gdalconst.gdalconst.GCI_AlphaBand
        ColorInterpretation.Hue -> org.gdal.gdalconst.gdalconst.GCI_HueBand
        ColorInterpretation.Saturation -> org.gdal.gdalconst.gdalconst.GCI_SaturationBand
        ColorInterpretation.Lightness -> org.gdal.gdalconst.gdalconst.GCI_LightnessBand
        ColorInterpretation.Cyan -> org.gdal.gdalconst.gdalconst.GCI_CyanBand
        ColorInterpretation.Magenta -> org.gdal.gdalconst.gdalconst.GCI_MagentaBand
        ColorInterpretation.Yellow -> org.gdal.gdalconst.gdalconst.GCI_YellowBand
        ColorInterpretation.Black -> org.gdal.gdalconst.gdalconst.GCI_BlackBand
        ColorInterpretation.YCbCr_Y -> org.gdal.gdalconst.gdalconst.GCI_YCbCr_YBand
        ColorInterpretation.YCbCr_Cr -> org.gdal.gdalconst.gdalconst.GCI_YCbCr_CrBand
        ColorInterpretation.YCbCr_Cb -> org.gdal.gdalconst.gdalconst.GCI_YCbCr_CbBand
    }

operator fun ColorInterpretation.Companion.get(gdalConst: Int): ColorInterpretation = when (gdalConst) {
    org.gdal.gdalconst.gdalconst.GCI_Undefined -> ColorInterpretation.Undefined
    org.gdal.gdalconst.gdalconst.GCI_GrayIndex -> ColorInterpretation.GrayIndex
    org.gdal.gdalconst.gdalconst.GCI_PaletteIndex -> ColorInterpretation.PaletteIndex
    org.gdal.gdalconst.gdalconst.GCI_RedBand -> ColorInterpretation.Red
    org.gdal.gdalconst.gdalconst.GCI_GreenBand -> ColorInterpretation.Green
    org.gdal.gdalconst.gdalconst.GCI_BlueBand -> ColorInterpretation.Blue
    org.gdal.gdalconst.gdalconst.GCI_AlphaBand -> ColorInterpretation.Alpha
    org.gdal.gdalconst.gdalconst.GCI_HueBand -> ColorInterpretation.Hue
    org.gdal.gdalconst.gdalconst.GCI_SaturationBand -> ColorInterpretation.Saturation
    org.gdal.gdalconst.gdalconst.GCI_LightnessBand -> ColorInterpretation.Lightness
    org.gdal.gdalconst.gdalconst.GCI_CyanBand -> ColorInterpretation.Cyan
    org.gdal.gdalconst.gdalconst.GCI_MagentaBand -> ColorInterpretation.Magenta
    org.gdal.gdalconst.gdalconst.GCI_YellowBand -> ColorInterpretation.Yellow
    org.gdal.gdalconst.gdalconst.GCI_BlackBand -> ColorInterpretation.Black
    org.gdal.gdalconst.gdalconst.GCI_YCbCr_YBand -> ColorInterpretation.YCbCr_Y
    org.gdal.gdalconst.gdalconst.GCI_YCbCr_CrBand -> ColorInterpretation.YCbCr_Cr
    org.gdal.gdalconst.gdalconst.GCI_YCbCr_CbBand -> ColorInterpretation.YCbCr_Cb
    else -> throw IllegalArgumentException("Invalid gdalconst for ColorInterpretation: $gdalConst")
}
