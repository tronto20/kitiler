package dev.tronto.titiler.image.outgoing.port

import dev.tronto.titiler.core.domain.Ordered
import dev.tronto.titiler.image.domain.ImageFormat

/**
 *  [ImageData] 중 DataType 이 맞지 않는 객체들을 자신이 알고 있는 [ImageDataRenderer]가 렌더링할 수 있도록 수정해주는 인터페이스
 */
interface ImageDataAutoRescale : Ordered {
    fun supports(imageData: ImageData, format: ImageFormat): Boolean

    fun rescale(imageData: ImageData, format: ImageFormat): ImageData
}
