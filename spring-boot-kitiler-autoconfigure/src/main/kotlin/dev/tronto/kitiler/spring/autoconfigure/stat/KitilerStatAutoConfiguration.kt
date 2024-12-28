package dev.tronto.kitiler.spring.autoconfigure.stat

import dev.tronto.kitiler.image.incoming.usecase.ImagePreviewUseCase
import dev.tronto.kitiler.spring.autoconfigure.image.KitilerImageAutoConfiguration
import dev.tronto.kitiler.spring.autoconfigure.utils.sortedByOrdered
import dev.tronto.kitiler.stat.incoming.usecase.StatisticsUseCase
import dev.tronto.kitiler.stat.outgoing.port.spi.ImageDataStatistics
import dev.tronto.kitiler.stat.service.StatisticsAutoRescale
import dev.tronto.kitiler.stat.service.StatisticsService
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.annotation.Order
import java.util.function.Supplier

@ComponentScan
@AutoConfiguration(after = [KitilerImageAutoConfiguration::class])
@EnableConfigurationProperties(KitilerStatPathProperties::class)
class KitilerStatAutoConfiguration(applicationContext: GenericApplicationContext) {
    init {
        ImageDataStatistics.services.forEach {
            applicationContext.defaultListableBeanFactory.registerBeanDefinition(
                it::class.qualifiedName ?: it.toString(),
                GenericBeanDefinition().apply {
                    setBeanClass(it::class.java)
                    instanceSupplier = Supplier { it }
                }
            )
        }
    }

    @Bean
    @ConditionalOnBean(ImagePreviewUseCase::class)
    fun statisticsService(
        imagePreviewUseCase: ImagePreviewUseCase,
        imageDataStatistics: ObjectProvider<ImageDataStatistics>,
    ) = StatisticsService(imagePreviewUseCase, imageDataStatistics.sortedByOrdered())

    @Bean
    @Order(-1)
    @ConditionalOnBean(StatisticsUseCase::class)
    fun statisticsAutoRescale(statisticsUseCase: StatisticsUseCase) = StatisticsAutoRescale(statisticsUseCase)
}
