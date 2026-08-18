package common.model.value.transformer

import common.model.value.Value
import common.type.option.Option

interface ValueTransformer {
    fun parse(input: String): Option<Value>
}
