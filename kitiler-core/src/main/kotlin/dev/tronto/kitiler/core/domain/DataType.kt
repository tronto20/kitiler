package dev.tronto.kitiler.core.domain

enum class DataType(val byteSize: Int) {
    Int8(Byte.SIZE_BYTES),
    UInt8(UByte.SIZE_BYTES),
    UInt16(UShort.SIZE_BYTES),
    Int16(Short.SIZE_BYTES),
    UInt32(UInt.SIZE_BYTES),
    Int32(Int.SIZE_BYTES),
    UInt64(ULong.SIZE_BYTES),
    Int64(Long.SIZE_BYTES),
    Float32(Float.SIZE_BYTES),
    Float64(Double.SIZE_BYTES),
    CInt16(Short.SIZE_BYTES),
    CInt32(Int.SIZE_BYTES),
    CFloat32(Float.SIZE_BYTES),
    CFloat64(Double.SIZE_BYTES),
    ;

    companion object
}
