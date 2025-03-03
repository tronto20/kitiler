package dev.tronto.kitiler.core.outgoing.adaptor.gdal

import dev.tronto.kitiler.core.domain.ColorInterpretation
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
import dev.tronto.kitiler.core.outgoing.port.CRSFactory
import dev.tronto.kitiler.core.utils.GdalInit
import dev.tronto.kitiler.image.outgoing.adaptor.gdal.gdalWarpString
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gdal.gdal.Dataset
import org.gdal.gdal.WarpOptions
import org.gdal.gdal.gdal
import org.gdal.gdalconst.gdalconst
import java.util.*
import kotlin.io.path.toPath

class GdalDatasetFactory(private val crsFactory: CRSFactory) {
    companion object {
        @JvmStatic
        private val logger = KotlinLogging.logger { }
    }

    private fun createVRT(
        crsOption: CRSOption?,
        noData: Double?,
        resamplingAlgorithmOption: ResamplingOption,
        rasterDataset: GdalDataset,
    ): GdalDataset {
        require(crsOption != null || noData != null)

        val resamplingAlgorithm = resamplingAlgorithmOption.algorithm

        /**
         *  /vsimem 은 모든 쓰레드에서 하나의 경로로 하나의 데이터셋을 공유할 수 있기에, 이름이 겹치는 일을 방지하기 위해 UUID 사용.
         */
        val memoryFile = "/vsimem/${UUID.randomUUID()}/${rasterDataset.name}.vrt"
        val warpOptions = mutableMapOf(
            "-of" to "VRT",
            "-r" to resamplingAlgorithm.gdalWarpString,
            "-dstalpha" to ""
        )
        if (crsOption != null) {
            warpOptions["-t_srs"] = crsOption.crsString
        }

        if (noData != null) {
            warpOptions.remove("-dstalpha")
            warpOptions["-srcnodata"] = noData.toString()
            warpOptions["-dstnodata"] = noData.toString()
        }

        val hasAlphaBand = (1..<rasterDataset.bandCount).reversed().any {
            val band = rasterDataset.dataset.GetRasterBand(it)
            ColorInterpretation[band.GetColorInterpretation()] == ColorInterpretation.Alpha
        }

        if (hasAlphaBand) {
            /**
             *  TODO :: mask flag 확인 필요
             *   any([MaskFlags.alpha in flags for flags in src_dst.mask_flag_enums])
             */
            warpOptions.remove("-dstalpha")
        }
        val options = warpOptions.flatMap { listOf(it.key, it.value) }.filter { it.isNotBlank() }
        val dataset = WarpOptions(Vector(options)).use {
            gdal.Warp(
                memoryFile,
                arrayOf(rasterDataset.dataset),
                it
            )
        }
        return GdalDataset(rasterDataset.name, dataset, memoryFile)
    }

    private fun createDataset(path: String): GdalDataset {
        val name = path.substringAfterLast('/')
            .substringBefore('?')
            .substringBeforeLast('.')
        val dataset: Dataset = try {
            val dataset: Dataset? = gdal.Open(path, gdalconst.GA_ReadOnly)
            dataset!!
        } catch (_: NullPointerException) {
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

        return GdalDataset(name, dataset)
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

    suspend fun createGdalDataset(options: OptionProvider<OpenOption>): GdalDataset {
        GdalInit
        val uriOption: URIOption = options.get()
        val uri = uriOption.uri
        val gdalPath = uri.tryToGdalPath(options)

        val openOptions = if (gdalPath != null) {
            options + gdalPath.openOptions
        } else {
            options
        }

        val path = if (gdalPath != null) {
            gdalPath.toPathString()
        } else if (uri.scheme == null) {
            uri.toString()
        } else {
            uri.toPath().toString()
        }
        val result = applyEnvs(openOptions) {
            val dataset = createDataset(path)
            kotlin.runCatching {
                val crsOption: CRSOption? = openOptions.getOrNull()
                val noDataOption: NoDataOption? = openOptions.getOrNull()
                val noData = noDataOption?.noData ?: dataset.noDataValue
                val crs = crsOption?.let { crsFactory.create(it.crsString) }
                if ((crs != null && !crs.isSame(dataset.getCrs(crsFactory))) || noData != null) {
                    dataset.use {
                        val resamplingAlgorithmOption: ResamplingOption = openOptions.get()
                        createVRT(
                            crsOption,
                            noData,
                            resamplingAlgorithmOption,
                            dataset
                        )
                    }
                } else {
                    dataset
                }
            }.onFailure {
                dataset.close()
            }.getOrThrow()
        }
        return result
    }
}
