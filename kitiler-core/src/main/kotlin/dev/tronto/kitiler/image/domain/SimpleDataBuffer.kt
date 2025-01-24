package dev.tronto.kitiler.image.domain

import dev.tronto.kitiler.core.domain.DataType
import java.nio.ByteBuffer

class SimpleDataBuffer(override val byteBuffer: ByteBuffer, override val dataType: DataType) : DataBuffer
