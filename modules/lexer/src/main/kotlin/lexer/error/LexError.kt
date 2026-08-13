package lexer.error

import common.model.diagnostic.Diagnostic
import common.model.diagnostic.category.Category
import common.model.diagnostic.severity.Error
import common.model.diagnostic.severity.Severity
import common.model.span.Span

internal data class LexError(
    override val message: String,
    val span: Span,
    override val category: Category,
    override val severity: Severity = Error,
) : Diagnostic {
    override fun format(): String =
        "${span.format()} -> ${severity.name} (${category.name}): $message"
}