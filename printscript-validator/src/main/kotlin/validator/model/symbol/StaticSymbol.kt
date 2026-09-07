package validator.model.symbol

import common.model.span.Span
import common.model.value.Value
import common.model.value.type.ValueType

internal data class StaticSymbol(
    val name: String,
    val value: Value,
    val declaredType: ValueType,
    val declaredAt: Span,
    val isMutable: Boolean,
)
