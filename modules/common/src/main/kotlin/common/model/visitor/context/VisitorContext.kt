package common.model.visitor.context

import common.type.option.Option
import common.type.option.Option.None
import common.type.option.Option.Some
import kotlin.reflect.KClass

class VisitorContext private constructor(private val registry: Map<KClass<*>, Any>) {
    constructor() : this(emptyMap())

    fun <T : Any> register(type: KClass<T>, instance: T): VisitorContext {
        return VisitorContext(registry + (type to instance))
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(type: KClass<T>): Option<T> = registry[type]?.let { Some(it as T) } ?: None
}
