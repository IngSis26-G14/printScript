package linter.visitor.factory

import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.rule.StringRuleValue
import common.model.visitor.Visitor
import linter.model.rule.IdentifierFormatRule
import linter.table.VisitorFactory
import linter.visitor.IdentifierFormatVisitor

internal class IdentifierFormatVisitorFactory : VisitorFactory {
    override val ruleType: RuleType = IdentifierFormatRule

    private val formatPatterns = mapOf(
        "snake case" to Regex("^[a-z]+(?:_[a-z0-9]+)*$"),
        "camel case" to Regex("^[a-z][a-zA-Z0-9]*$"),
    )

    override fun create(rule: Rule): Visitor {
        val format = (rule.value as StringRuleValue).value.lowercase()
        val regex = formatPatterns[format] ?: Regex(".*")
        return IdentifierFormatVisitor(format, regex)
    }
}
