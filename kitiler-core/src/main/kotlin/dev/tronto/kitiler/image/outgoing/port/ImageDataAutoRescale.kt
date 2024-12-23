package dev.tronto.kitiler.image.outgoing.port

import dev.tronto.kitiler.core.domain.Ordered
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.ImageFormat
import java.util.*

/**
 *  [ImageData] 중 DataType 이 맞지 않는 객체들을 자신이 알고 있는 [ImageRenderer]가 렌더링할 수 있도록 수정해주는 인터페이스
 */
interface ImageDataAutoRescale {
    fun supports(imageData: ImageData, format: ImageFormat): Boolean

    suspend fun rescale(imageData: ImageData, format: ImageFormat): ImageData

    companion object {
        @JvmStatic
        val services by lazy {
            ServiceLoader.load(ImageDataAutoRescale::class.java, Thread.currentThread().contextClassLoader)
                .sortedBy { if (it is Ordered) it.order else 0 }.toList()
        }
    }
}
