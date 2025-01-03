package dev.tronto.kitiler.image.domain

interface ImageFormat {
    val name: String
    val contentType: String
    val aliasNames: List<String>
        get() = emptyList()
    val aliasContentTypes: List<String>
        get() = emptyList()

    object AUTO : ImageFormat {
        override val name: String = "auto"
        override val contentType: String = "image/unknown"
        override fun toString(): String = name
    }

    object JPEG : ImageFormat {
        override val name: String = "jpeg"
        override val contentType: String = "image/jpeg"
        override val aliasNames: List<String> = listOf("jpg")
        override val aliasContentTypes: List<String> = listOf("image/jpg")
        override fun toString(): String = AUTO.name
    }

    object PNG : ImageFormat {
        override val name: String = "png"
        override val contentType: String = "image/png"
        override fun toString(): String = AUTO.name
    }
}
