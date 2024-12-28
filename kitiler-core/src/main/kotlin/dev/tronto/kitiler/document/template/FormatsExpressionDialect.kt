package dev.tronto.kitiler.document.template

import org.thymeleaf.context.IExpressionContext
import org.thymeleaf.dialect.IExpressionObjectDialect
import org.thymeleaf.expression.IExpressionObjectFactory

class FormatsExpressionDialect : IExpressionObjectDialect {
    override fun getName(): String = "formats"

    override fun getExpressionObjectFactory(): IExpressionObjectFactory = object : IExpressionObjectFactory {
        override fun getAllExpressionObjectNames(): MutableSet<String> = mutableSetOf("formats")

        override fun buildObject(context: IExpressionContext, expressionObjectName: String?): Any? =
            if (expressionObjectName?.equals("formats") == true) {
                Formats(context.locale)
            } else {
                null
            }

        override fun isCacheable(expressionObjectName: String?): Boolean = true
    }
}
