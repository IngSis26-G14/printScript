package common.model.diagnostic

import common.model.diagnostic.category.Category
import common.model.diagnostic.severity.Severity
import common.model.span.Span

interface Diagnostic {
    val span: Span? get() = null
    val message: String
    val severity: Severity
    val category: Category
    fun format(): String = "${severity.name} -> (${category.name}): $message"
}
