package dev.tronto.kitiler.image.outgoing.port

import dev.tronto.kitiler.core.domain.Ordered
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.ImageFormat
import java.util.*

/**
 *  [ImageData] 중 자신이 가능한 객체를 렌더링 해주는 인터페이스
 */
interface ImageRenderer {
    fun supports(imageData: ImageData, format: ImageFormat): Boolean

    suspend fun render(imageData: ImageData, format: ImageFormat): ByteArray

    companion object {
        @JvmStatic
        val services by lazy {
            ServiceLoader.load(ImageRenderer::class.java, Thread.currentThread().contextClassLoader)
                .sortedBy { if (it is Ordered) it.order else 0 }.toList()
        }
    }
}
