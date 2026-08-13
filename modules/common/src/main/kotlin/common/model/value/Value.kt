package common.model.value

import common.model.value.type.ValueType

interface Value {
    val type: ValueType
    fun format(): String
}
