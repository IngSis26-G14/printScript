package formatter.visitor.factory

import common.model.rule.BooleanRuleValue
import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.context.ContextVisitor
import formatter.rule.SpacingBeforeColonRule
import formatter.visitor.SpacingBeforeColonVisitor

internal class SpacingBeforeColonVisitorFactory : ContextVisitorFactory {
    override val ruleType: RuleType = SpacingBeforeColonRule

    override fun create(rule: Rule): ContextVisitor {
        val enforce = (rule.value as BooleanRuleValue).value
        return SpacingBeforeColonVisitor(enforce)
    }
}
