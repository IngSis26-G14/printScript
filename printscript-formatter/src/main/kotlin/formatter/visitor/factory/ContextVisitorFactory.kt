package formatter.visitor.factory

import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.context.ContextVisitor

internal interface ContextVisitorFactory {
    val ruleType: RuleType
    fun create(rule: Rule): ContextVisitor
}
