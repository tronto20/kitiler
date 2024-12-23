package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.exception.GdalDatasetOpenFailedException
import dev.tronto.kitiler.core.incoming.controller.option.CRSOption
import dev.tronto.kitiler.core.incoming.controller.option.EnvOption
import dev.tronto.kitiler.core.incoming.controller.option.NoDataOption
import dev.tronto.kitiler.core.incoming.controller.option.OpenOption
import dev.tronto.kitiler.core.incoming.controller.option.OptionProvider
import dev.tronto.kitiler.core.incoming.controller.option.ResamplingOption
import dev.tronto.kitiler.core.incoming.controller.option.URIOption
import dev.tronto.kitiler.core.incoming.controller.option.get
import dev.tronto.kitiler.core.incoming.controller.option.getAll
import dev.tronto.kitiler.core.incoming.controller.option.getOrNull
import dev.tronto.kitiler.core.outgoing.adaptor.gdal.path.tryToGdalPath
import dev.tronto.kitiler.core.outgoing.port.CRS
import dev.tronto.kitiler.core.outgoing.port.CRSFactory
import dev.tronto.kitiler.core.outgoing.port.Raster
import dev.tronto.kitiler.core.outgoing.port.RasterFactory
import dev.tronto.kitiler.image.outgoing.adaptor.gdal.gdalWarpString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.gdal.gdal.Dataset
import org.gdal.gdal.WarpOptions
import org.gdal.gdal.gdal
import org.gdal.gdalconst.gdalconst
import org.gdal.ogr.ogr
import org.gdal.osr.osr
import java.util.*
import kotlin.io.path.toPath

class GdalRasterFactory(private val crsFactory: CRSFactory = SpatialReferenceCRSFactory) : RasterFactory {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }

        @JvmStatic
        private val dispatcher =
            Dispatchers.IO.limitedParallelism(
                maxOf(4, Runtime.getRuntime().availableProcessors() * 2 - 1)
            )
    }
    private object GdalInit {
        init {
            gdal.AllRegister()
            gdal.UseExceptions()
            ogr.UseExceptions()
            osr.UseExceptions()
        }
    }

    private fun createVRT(openOptions: OptionProvider<OpenOption>, raster: GdalRaster): GdalBaseRaster? {
        val crsOption: CRSOption? = openOptions.getOrNull()
        val noDataOption: NoDataOption? = openOptions.getOrNull()
        val noData = noDataOption?.noData ?: raster.noDataValue

        return if (crsOption != null || noData != null) {
            val resamplingAlgorithmOption: ResamplingOption = openOptions.get()
            val resamplingAlgorithm = resamplingAlgorithmOption.algorithm
            val memoryFile = "/vsimem/${raster.name}.vrt"
            val warpOptions = mutableMapOf(
                "-of" to "VRT",
                "-r" to resamplingAlgorithm.gdalWarpString,
                "-dstalpha" to ""
            )
            if (crsOption != null) {
                val targetCRS: CRS = crsFactory.create(crsOption.crsString)
                warpOptions["-t_srs"] = targetCRS.proj4
            }

            if (noData != null) {
                warpOptions.remove("-dstalpha")
                warpOptions["-srcnodata"] = noData.toString()
                warpOptions["-dstnodata"] = noData.toString()
            }

            if (raster.hasAlphaBand()) {
                /**
                 *  TODO :: mask flag 확인 필요
                 *   any([MaskFlags.alpha in flags for flags in src_dst.mask_flag_enums])
                 */
                warpOptions.remove("-dstalpha")
            }
            val options = warpOptions.flatMap { listOf(it.key, it.value) }.filter { it.isNotBlank() }
            val warpOption = WarpOptions(Vector(options))
            val dataset = try {
                gdal.Warp(
                    memoryFile,
                    arrayOf(raster.dataset),
                    warpOption
                )
            } finally {
                kotlin.runCatching { warpOption.delete() }.onFailure {
                    logger.warn(it) { "cannot delete WarpOption." }
                }
            }

            GdalMemFileRaster(GdalRaster(dataset, crsFactory, raster.name), memoryFile)
        } else {
            null
        }
    }

    private fun createRaster(path: String, openOptions: OptionProvider<OpenOption>): GdalRaster {
        val dataset: Dataset = try {
            val dataset: Dataset? = gdal.Open(path, gdalconst.GA_ReadOnly)
            dataset!!
        } catch (e: NullPointerException) {
            throw GdalDatasetOpenFailedException(
                path,
                RuntimeException(
                    if (path.startsWith("/vsi")) {
                        gdal.VSIGetLastErrorMsg()
                    } else {
                        gdal.GetLastErrorMsg()
                    }
                )
            )
        } catch (e: RuntimeException) {
            throw GdalDatasetOpenFailedException(path, e)
        }
        return GdalRaster(
            dataset,
            crsFactory,
            path.substringAfterLast('/')
                .substringBefore('?')
                .substringBeforeLast('.')
        )
    }

    private fun <T> applyEnvs(openOptions: OptionProvider<OpenOption>, block: () -> T): T {
        val envOptions: List<EnvOption> = openOptions.getAll()
        return try {
            envOptions.forEach {
                gdal.SetThreadLocalConfigOption(it.key, it.value)
            }
            block()
        } finally {
            envOptions.forEach {
                gdal.SetThreadLocalConfigOption(it.key, null)
            }
        }
    }

    suspend fun <T> withGdalRaster(openOptions: OptionProvider<OpenOption>, block: (raster: GdalBaseRaster) -> T): T {
        GdalInit
        val uriOption: URIOption = openOptions.get()
        val uri = uriOption.uri
        val gdalPath = uri.tryToGdalPath(openOptions)

        val newOpenOptions = if (gdalPath != null) {
            openOptions + gdalPath.openOptions
        } else {
            openOptions
        }

        val path = if (gdalPath != null) {
            gdalPath.toPathString()
        } else if (uri.scheme == null) {
            uri.toString()
        } else {
            uri.toPath().toString()
        }
        return withContext(dispatcher) {
            applyEnvs(newOpenOptions) {
                createRaster(path, newOpenOptions).use { raster ->
                    createVRT(newOpenOptions, raster)?.use(block) ?: block(raster)
                }
            }
        }
    }

    /**
     *  GdalDataset 사용을 하나의 쓰레드에서 진행하도록 강제
     */
    override suspend fun <T> withRaster(openOptions: OptionProvider<OpenOption>, block: (raster: Raster) -> T): T =
        withGdalRaster(openOptions) {
            block(it)
        }
}
