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
        override fun toString(): String = name
    }

    object PNG : ImageFormat {
        override val name: String = "png"
        override val aliasNames: List<String> = listOf("pngraw")
        override val contentType: String = "image/png"
        override fun toString(): String = name
    }

    object NPY : ImageFormat {
        override val name: String = "npy"
        override val contentType: String = "application/x-npy"
        override fun toString(): String = name
    }

    object NPZ : ImageFormat {
        override val name: String = "npz"
        override val contentType: String = "application/x-npz"
        override fun toString(): String = name
    }

    object WEBP : ImageFormat {
        override val name: String = "webp"
        override val contentType: String = "image/webp"
        override fun toString(): String = name
    }

    object JP2 : ImageFormat {
        override val name: String = "jp2"
        override val contentType: String = "image/x-jp2"
        override fun toString(): String = name
    }
}
