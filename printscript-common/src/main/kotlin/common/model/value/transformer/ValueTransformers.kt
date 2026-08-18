package common.model.value.transformer

import common.model.value.BooleanValue
import common.model.value.FloatValue
import common.model.value.IntegerValue
import common.model.value.StringValue
import common.model.value.Value
import common.type.option.Option

object IntegerValueTransformer : ValueTransformer {
    override fun parse(input: String): Option<Value> {
        val result = input.toIntOrNull()
        return when (result) {
            null -> {
                Option.None
            }
            else -> {
                Option.Some(IntegerValue(result))
            }
        }
    }
}

object FloatValueTransformer : ValueTransformer {
    override fun parse(input: String): Option<Value> {
        val result = input.toFloatOrNull()
        return when (result) {
            null -> {
                Option.None
            }
            else -> {
                Option.Some(FloatValue(result))
            }
        }
    }
}

object BooleanValueTransformer : ValueTransformer {
    override fun parse(input: String): Option<Value> =
        when (input.lowercase()) {
            "true" -> {
                Option.Some(BooleanValue(true))
            }
            "false" -> {
                Option.Some(BooleanValue(false))
            }
            else -> {
                Option.None
            }
        }
}

object StringValueTransformer : ValueTransformer {
    override fun parse(input: String): Option<Value> = Option.Some(StringValue(input))
}
