package dev.tronto.kitiler.core.incoming.controller.option

import org.springframework.core.ParameterizedTypeReference
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

class ArgumentType<T : Any>(val kType: KType, val javaType: Type) {

    companion object {
        inline operator fun <reified T : Any> invoke(): ArgumentType<T> {
            // fixme :: KType 만 했을 때에는 native-image 에서 Java 로 선언한 Class 에서 에러가 발생함.
            val typeReference: ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}
            return ArgumentType(
                typeOf<T>(),
                typeReference.type
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArgumentType<*>) return false
        return kType == other.kType
    }

    override fun hashCode(): Int = kType.hashCode()
    override fun toString(): String = kType.toString()

    fun isSubtypeOf(other: ArgumentType<*>): Boolean = kType.isSubtypeOf(other.kType)
}
