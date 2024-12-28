package dev.tronto.kitiler.spring.autoconfigure.image

import dev.tronto.kitiler.image.domain.Image
import org.reactivestreams.Publisher
import org.springframework.core.ResolvableType
import org.springframework.http.MediaType
import org.springframework.http.ReactiveHttpOutputMessage
import org.springframework.http.codec.HttpMessageWriter
import reactor.core.publisher.Mono

class DefaultImageHttpMessageWriter : HttpMessageWriter<Image> {
    companion object {
        @JvmStatic
        private val imageType = ResolvableType.forClass(Image::class.java)
    }

    override fun getWritableMediaTypes(): MutableList<MediaType> = emptyList<MediaType>().toMutableList()

    override fun canWrite(elementType: ResolvableType, mediaType: MediaType?): Boolean =
        mediaType == null && imageType.isAssignableFrom(elementType)

    override fun write(
        inputStream: Publisher<out Image>,
        elementType: ResolvableType,
        mediaType: MediaType?,
        message: ReactiveHttpOutputMessage,
        hints: MutableMap<String, Any>,
    ): Mono<Void> = if (inputStream is Mono<out Image>) {
        inputStream.flatMap {
            val mediaType = MediaType.parseMediaType(it.format.contentType)
            message.headers.contentType = mediaType
            message.writeWith(
                Mono.just(message.bufferFactory().wrap(it.data))
            )
        }
    } else {
        throw UnsupportedOperationException()
    }
}
