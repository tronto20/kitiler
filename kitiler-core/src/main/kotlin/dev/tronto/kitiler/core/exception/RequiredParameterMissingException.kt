package dev.tronto.kitiler.core.exception

class RequiredParameterMissingException(vararg val parameterName: String) :
    IllegalParameterException(
        "Parameter ${parameterName.toList()} is required."
    )
