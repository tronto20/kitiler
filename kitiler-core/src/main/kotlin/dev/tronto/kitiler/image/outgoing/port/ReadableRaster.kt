package dev.tronto.kitiler.image.outgoing.port

import dev.tronto.kitiler.core.domain.BandIndex
import dev.tronto.kitiler.core.outgoing.port.Raster
import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.Window

interface ReadableRaster : Raster {
    /**
     *  읽기
     *  @param window 읽을 영역
     *  @param width 읽은 이미지의 가로 길이
     *  @param height 읽은 이미지의 세로 길이
     *  @param bandIndexes 이미지의 밴드 순서, null 일 경우 기본 순서 (1, 2, 3..)
     *  @param nodata 이미지의 noData 값을 override, null 일 경우 이미지의 noData 사용
     */
    fun read(
        window: Window,
        width: Int,
        height: Int,
        bandIndexes: List<BandIndex>? = null,
        nodata: Number? = null,
    ): ImageData
}
