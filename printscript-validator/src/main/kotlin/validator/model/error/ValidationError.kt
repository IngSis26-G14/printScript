package validator.model.error

import common.model.diagnostic.Diagnostic
import common.model.diagnostic.category.Category
import common.model.diagnostic.severity.Error
import common.model.diagnostic.severity.Severity
import common.model.span.Span

internal data class ValidationError(
    override val message: String,
    override val category: Category,
    val span: Span,
    override val severity: Severity = Error,
) : Diagnostic {
    override fun format(): String {
        return "${span.format()} -> ${severity.name} (${category.name}): $message"
    }
}
