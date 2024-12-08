package dev.tronto.titiler.document.template

import org.thymeleaf.IEngineConfiguration
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver
import org.thymeleaf.templateresource.ITemplateResource

class ClasspathFileTemplateResolver : AbstractConfigurableTemplateResolver() {
    override fun computeTemplateResource(
        configuration: IEngineConfiguration,
        ownerTemplate: String?,
        template: String?,
        resourceName: String,
        characterEncoding: String?,
        templateResolutionAttributes: MutableMap<String, Any>?,
    ): ITemplateResource {
        return ClasspathFileTemplateResource(resourceName, characterEncoding)
    }
}
