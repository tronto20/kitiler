package dev.tronto.kitiler.core.domain

enum class ColorInterpretation {
    Undefined,
    GrayIndex,
    PaletteIndex,
    RedBand,
    GreenBand,
    BlueBand,
    AlphaBand,
    HueBand,
    SaturationBand,
    LightnessBand,
    CyanBand,
    MagentaBand,
    YellowBand,
    BlackBand,

    @Suppress("EnumEntryName")
    YCbCr_YBand,

    @Suppress("EnumEntryName")
    YCbCr_CrBand,

    @Suppress("EnumEntryName")
    YCbCr_CbBand,
    ;

    companion object
}
