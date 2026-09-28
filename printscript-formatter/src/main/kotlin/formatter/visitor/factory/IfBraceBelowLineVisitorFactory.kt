package formatter.visitor.factory

import common.model.rule.BooleanRuleValue
import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.context.ContextVisitor
import formatter.rule.IfBraceBelowLineRule
import formatter.visitor.IfBraceBelowLineVisitor

internal class IfBraceBelowLineVisitorFactory : ContextVisitorFactory {
    override val ruleType: RuleType = IfBraceBelowLineRule

    override fun create(rule: Rule): ContextVisitor =
        IfBraceBelowLineVisitor((rule.value as BooleanRuleValue).value)
}
