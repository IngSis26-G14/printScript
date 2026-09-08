package formatter.visitor.factory

import common.model.rule.BooleanRuleValue
import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.context.ContextVisitor
import formatter.rule.SpacingAfterColonRule
import formatter.visitor.SpacingAfterColonVisitor

internal class SpacingAfterColonVisitorFactory : ContextVisitorFactory {
    override val ruleType: RuleType = SpacingAfterColonRule

    override fun create(rule: Rule): ContextVisitor {
        val enforce = (rule.value as BooleanRuleValue).value
        return SpacingAfterColonVisitor(enforce)
    }
}
