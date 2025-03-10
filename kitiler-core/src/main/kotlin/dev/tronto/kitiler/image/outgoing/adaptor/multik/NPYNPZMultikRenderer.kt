package dev.tronto.kitiler.image.outgoing.adaptor.multik

import dev.tronto.kitiler.image.domain.ImageData
import dev.tronto.kitiler.image.domain.ImageFormat
import dev.tronto.kitiler.image.outgoing.port.ImageRenderer
import org.jetbrains.bio.npy.NpzFile
import org.jetbrains.kotlinx.multik.api.Multik
import org.jetbrains.kotlinx.multik.api.io.writeNPY
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.DataType
import org.jetbrains.kotlinx.multik.ndarray.data.Dimension
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import java.net.URI
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.ReadableByteChannel
import java.nio.channels.SeekableByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.file.AccessMode
import java.nio.file.CopyOption
import java.nio.file.DirectoryStream
import java.nio.file.FileStore
import java.nio.file.FileSystem
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider
import kotlin.math.min

class NPYNPZMultikRenderer : ImageRenderer {
    companion object {
        @JvmStatic
        private val SUPPORT_FORMAT = listOf(ImageFormat.NPY, ImageFormat.NPZ)
    }

    override fun supports(imageData: ImageData, format: ImageFormat): Boolean = format in SUPPORT_FORMAT

    override suspend fun render(imageData: ImageData, format: ImageFormat): ByteArray {
        val data = imageData.getBandBuffer()
        val valid = imageData.getValidArray()
        val tmpFilePath = InMemoryMockPath("image")
        if (valid == null) {
            val mkData = when {
                data.isIntArray -> {
                    mk.ndarray(data.intArray, imageData.bandCount, imageData.height, imageData.width)
                }

                data.isLongArray -> {
                    mk.ndarray(data.longArray, imageData.bandCount, imageData.height, imageData.width)
                }

                data.isFloatArray -> {
                    mk.ndarray(data.floatArray, imageData.bandCount, imageData.height, imageData.width)
                }

                data.isDoubleArray -> {
                    mk.ndarray(data.doubleArray, imageData.bandCount, imageData.height, imageData.width)
                }

                else -> throw IllegalStateException("Unsupported data dataType: ${data.dataType}")
            }
            if (format == ImageFormat.NPY) {
                mk.writeNPY(tmpFilePath, mkData)
            } else if (format == ImageFormat.NPZ) {
                mk.writeNPZ(tmpFilePath, mapOf("data" to mkData))
            }
        } else {
            if (format == ImageFormat.NPY) {
                val npyData = when {
                    data.isIntArray -> {
                        val resultDataArray = data.intArray
                        val resultValidArray = IntArray(valid.size) {
                            if (valid[it]) 255 else 0
                        }
                        mk.ndarray(
                            resultDataArray + resultValidArray,
                            imageData.bandCount + 1,
                            imageData.height,
                            imageData.width
                        )
                    }

                    data.isLongArray -> {
                        val resultDataArray = data.longArray
                        val resultValidArray = LongArray(valid.size) {
                            if (valid[it]) 255 else 0
                        }

                        mk.ndarray(
                            resultDataArray + resultValidArray,
                            imageData.bandCount + 1,
                            imageData.height,
                            imageData.width
                        )
                    }

                    data.isFloatArray -> {
                        val resultDataArray = data.floatArray
                        val resultValidArray = FloatArray(valid.size) {
                            if (valid[it]) 255f else 0f
                        }
                        mk.ndarray(
                            resultDataArray + resultValidArray,
                            imageData.bandCount + 1,
                            imageData.height,
                            imageData.width
                        )
                    }

                    data.isDoubleArray -> {
                        val resultDataArray = data.doubleArray
                        val resultValidArray = DoubleArray(valid.size) {
                            if (valid[it]) 255.0 else 0.0
                        }
                        mk.ndarray(
                            resultDataArray + resultValidArray,
                            imageData.bandCount + 1,
                            imageData.height,
                            imageData.width
                        )
                    }

                    else -> throw IllegalStateException("Unsupported data dataType: ${data.dataType}")
                }
                mk.writeNPY(tmpFilePath, npyData)
            } else if (format == ImageFormat.NPZ) {
                val mkData = when {
                    data.isIntArray -> {
                        mk.ndarray(data.intArray, imageData.bandCount, imageData.height, imageData.width)
                    }

                    data.isLongArray -> {
                        mk.ndarray(data.longArray, imageData.bandCount, imageData.height, imageData.width)
                    }

                    data.isFloatArray -> {
                        mk.ndarray(data.floatArray, imageData.bandCount, imageData.height, imageData.width)
                    }

                    data.isDoubleArray -> {
                        mk.ndarray(data.doubleArray, imageData.bandCount, imageData.height, imageData.width)
                    }

                    else -> throw IllegalStateException("Unsupported data dataType: ${data.dataType}")
                }
                val validArray = IntArray(valid.size)
                for (i in valid.indices) {
                    validArray[i] = if (valid[i]) {
                        255
                    } else {
                        0
                    }
                }
                val ndValid = mk.ndarray(validArray, imageData.height, imageData.width)
                mk.writeNPZ(tmpFilePath, mapOf("data" to mkData, "mask" to ndValid))
            } else {
                throw UnsupportedOperationException("$format format not supported.")
            }
        }
        return tmpFilePath.byteArray
    }

    private fun Multik.writeNPZ(path: Path, ndArrays: Map<String, NDArray<out Number, out Dimension>>) {
        NpzFile.write(path).use {
            ndArrays.forEach { name, array ->
                when (array.dtype) {
                    DataType.DoubleDataType -> it.write(name, array.data.getDoubleArray(), array.shape)
                    DataType.FloatDataType -> it.write(name, array.data.getFloatArray(), array.shape)
                    DataType.IntDataType -> it.write(name, array.data.getIntArray(), array.shape)
                    DataType.LongDataType -> it.write(name, array.data.getLongArray(), array.shape)
                    DataType.ShortDataType -> it.write(name, array.data.getShortArray(), array.shape)
                    DataType.ByteDataType -> it.write(name, array.data.getByteArray(), array.shape)
                    else -> throw IllegalArgumentException(
                        "Unsupported data type: ${array.dtype}. Only Double, Float, Int, Long, Short and Byte are supported."
                    )
                }
            }
        }
    }
}

private class InMemoryMockPath(
    val name: String,
    var byteArray: ByteArray = ByteArray(5 * 1024), // 5 kB
) : Path {
    override fun getFileSystem(): FileSystem = InMemoryFileSystem()

    override fun isAbsolute(): Boolean = true

    override fun getRoot(): Path? = throw NotImplementedError()

    override fun getFileName(): Path? = throw NotImplementedError()

    override fun getParent(): Path? = throw NotImplementedError()

    override fun getNameCount(): Int = throw NotImplementedError()

    override fun getName(index: Int): Path = throw NotImplementedError()

    override fun subpath(beginIndex: Int, endIndex: Int): Path = throw NotImplementedError()

    override fun startsWith(other: Path): Boolean = throw NotImplementedError()

    override fun endsWith(other: Path): Boolean = throw NotImplementedError()

    override fun normalize(): Path = this

    override fun resolve(other: Path): Path = throw NotImplementedError()

    override fun relativize(other: Path): Path = throw NotImplementedError()

    override fun toUri(): URI = URI.create("${InMemoryFileSystemProvider.SCHEME}://$name")

    override fun toAbsolutePath(): Path = this

    override fun toRealPath(vararg options: LinkOption): Path = this

    override fun register(
        watcher: WatchService,
        events: Array<out WatchEvent.Kind<*>>?,
        vararg modifiers: WatchEvent.Modifier?,
    ): WatchKey = throw NotImplementedError()

    override fun compareTo(other: Path): Int = throw NotImplementedError()
}

private class InMemoryFileSystemProvider : FileSystemProvider() {
    companion object {
        @JvmStatic
        val INSTANCE = InMemoryFileSystemProvider()
        const val SCHEME = "in-memory"
    }

    override fun getScheme(): String = SCHEME

    override fun <A : BasicFileAttributes?> readAttributes(
        path: Path?,
        type: Class<A?>?,
        vararg options: LinkOption?,
    ): A? = throw NotImplementedError()

    override fun readAttributes(path: Path?, attributes: String?, vararg options: LinkOption?): Map<String?, Any?>? =
        throw NotImplementedError()

    override fun delete(path: Path?) = Unit

    override fun <V : FileAttributeView?> getFileAttributeView(
        path: Path?,
        type: Class<V?>?,
        vararg options: LinkOption?,
    ): V? = throw NotImplementedError()

    override fun setAttribute(path: Path?, attribute: String?, value: Any?, vararg options: LinkOption?): Unit =
        throw NotImplementedError()

    override fun newFileSystem(uri: URI?, env: Map<String?, *>?): FileSystem? = InMemoryFileSystem()

    override fun getFileSystem(uri: URI?): FileSystem? = InMemoryFileSystem()

    override fun getPath(uri: URI): Path = InMemoryMockPath(uri.toASCIIString())

    override fun newByteChannel(
        path: Path?,
        options: Set<OpenOption?>?,
        vararg attrs: FileAttribute<*>?,
    ): SeekableByteChannel = newFileChannel(path, options, attrs = attrs)

    override fun newDirectoryStream(dir: Path?, filter: DirectoryStream.Filter<in Path>?): DirectoryStream<Path?>? =
        throw NotImplementedError()

    override fun createDirectory(dir: Path?, vararg attrs: FileAttribute<*>?): Unit = throw NotImplementedError()

    override fun copy(source: Path?, target: Path?, vararg options: CopyOption?): Unit = throw NotImplementedError()

    override fun move(source: Path?, target: Path?, vararg options: CopyOption?): Unit = throw NotImplementedError()

    override fun isSameFile(path: Path?, path2: Path?): Boolean = throw NotImplementedError()

    override fun isHidden(path: Path?): Boolean = throw NotImplementedError()

    override fun getFileStore(path: Path?): FileStore? = throw NotImplementedError()

    override fun checkAccess(path: Path?, vararg modes: AccessMode?): Unit = throw NotImplementedError()

    override fun newFileChannel(
        path: Path?,
        options: Set<OpenOption?>?,
        vararg attrs: FileAttribute<*>?,
    ): FileChannel {
        require(path is InMemoryMockPath)
        return ByteArrayFileChannel(path)
    }
}

private class ByteArrayFileChannel(private val path: InMemoryMockPath, private var position: Int = 0) :
    FileChannel() {
    private var byteArray: ByteArray
        get() = path.byteArray
        set(value) {
            path.byteArray = value
        }

    override fun position(): Long = position.toLong()

    override fun position(newPosition: Long): FileChannel? {
        position = newPosition.toInt()
        return this
    }

    override fun write(src: ByteBuffer?): Int {
        val size = src?.remaining() ?: return 0
        if (size > byteArray.size - position) {
            expandByteArray(position + size)
        }
        src.get(byteArray, position, size)
        position += size
        return size
    }

    private fun expandByteArray(size: Int) {
        val newByteArray = ByteArray(size)
        byteArray.copyInto(newByteArray, 0, 0, min(byteArray.size, newByteArray.size))
        byteArray = newByteArray
    }

    override fun truncate(size: Long): FileChannel? {
        if (size.toInt() != byteArray.size) {
            expandByteArray(size.toInt())
        }
        return this
    }

    override fun size(): Long = byteArray.size.toLong()

    override fun tryLock(position: Long, size: Long, shared: Boolean): FileLock? = throw NotImplementedError()

    override fun lock(position: Long, size: Long, shared: Boolean): FileLock? = throw NotImplementedError()

    override fun implCloseChannel(): Unit = Unit

    override fun transferFrom(src: ReadableByteChannel?, position: Long, count: Long): Long =
        throw NotImplementedError()

    override fun map(mode: MapMode?, position: Long, size: Long): MappedByteBuffer? = throw NotImplementedError()

    override fun transferTo(position: Long, count: Long, target: WritableByteChannel?): Long =
        throw NotImplementedError()

    override fun write(srcs: Array<out ByteBuffer?>?, offset: Int, length: Int): Long = throw NotImplementedError()

    override fun write(src: ByteBuffer?, position: Long): Int = throw NotImplementedError()

    override fun read(dst: ByteBuffer): Int {
        dst.put(byteArray)
        return byteArray.size
    }

    override fun read(dsts: Array<out ByteBuffer?>?, offset: Int, length: Int): Long = throw NotImplementedError()

    override fun read(dst: ByteBuffer?, position: Long): Int = throw NotImplementedError()

    override fun force(metaData: Boolean): Unit = throw NotImplementedError()
}

private class InMemoryFileSystem : FileSystem() {
    override fun provider(): FileSystemProvider = InMemoryFileSystemProvider.INSTANCE

    override fun close() = Unit

    override fun isOpen(): Boolean = true

    override fun isReadOnly(): Boolean = false

    override fun getSeparator(): String = "/"

    override fun getRootDirectories(): Iterable<Path?>? = throw NotImplementedError()

    override fun getFileStores(): Iterable<FileStore?>? = throw NotImplementedError()

    override fun supportedFileAttributeViews(): Set<String?>? = throw NotImplementedError()

    override fun getPath(first: String, vararg more: String?): Path = throw NotImplementedError()

    override fun getPathMatcher(syntaxAndPattern: String?): PathMatcher? = throw NotImplementedError()

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService? = throw NotImplementedError()

    override fun newWatchService(): WatchService? = throw NotImplementedError()
}
