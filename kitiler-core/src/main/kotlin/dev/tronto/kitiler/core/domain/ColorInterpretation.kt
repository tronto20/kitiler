package dev.tronto.kitiler.core.domain

@Suppress("EnumEntryName")
enum class ColorInterpretation {
    Undefined,
    GrayIndex,
    PaletteIndex,
    Red,
    Green,
    Blue,
    Alpha,
    Hue,
    Saturation,
    Lightness,
    Cyan,
    Magenta,
    Yellow,
    Black,
    YCbCr_Y,
    YCbCr_Cr,
    YCbCr_Cb,
    ;

    companion object
}
