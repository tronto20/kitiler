package dev.tronto.kitiler.image.outgoing.adaptor.imageio

import javax.imageio.ImageIO

/**
 *  native-image 에서 ImageIO plugin 을 사용하기 위해 클래스 로드 시도.
 */
object ImageIORegistrar {
    init {
        ImageIO.scanForPlugins()
    }
}
