package linter.model.lint

import common.model.diagnostic.Diagnostic
import common.model.diagnostic.category.Category
import common.model.diagnostic.severity.Severity
import common.model.rule.RuleType
import common.model.span.Span
import linter.model.origin.Lint

internal data class Lint(
    override val message: String,
    val span: Span,
    val rule: RuleType,
    override val severity: Severity,
    override val category: Category = Lint,
) : Diagnostic {
    override fun format(): String {
        return "${span.format()} -> $severity (${category.name}): [$rule] $message"
    }
}
