package validator.model.value

import common.model.value.Value
import common.model.value.type.ValueType

internal class RuntimeValue(
    override val type: ValueType,
) : Value {
    override fun format(): String = ""
}
