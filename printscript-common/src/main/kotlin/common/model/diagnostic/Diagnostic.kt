package common.model.diagnostic

import common.model.diagnostic.category.Category
import common.model.diagnostic.severity.Severity

interface Diagnostic {
    val message: String
    val severity: Severity
    val category: Category
    fun format(): String = "${severity.name} -> (${category.name}): $message"
}
