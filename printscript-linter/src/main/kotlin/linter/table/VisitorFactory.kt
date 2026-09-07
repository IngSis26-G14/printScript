package linter.table

import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.Visitor

internal interface VisitorFactory {
    val ruleType: RuleType
    fun create(rule: Rule): Visitor
}
