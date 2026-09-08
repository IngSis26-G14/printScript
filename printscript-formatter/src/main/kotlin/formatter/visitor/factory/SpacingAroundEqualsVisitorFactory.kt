package formatter.visitor.factory

import common.model.rule.BooleanRuleValue
import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.context.ContextVisitor
import formatter.rule.SpacingAroundEqualsRule
import formatter.visitor.SpacingAroundEqualsVisitor

internal class SpacingAroundEqualsVisitorFactory : ContextVisitorFactory {
    override val ruleType: RuleType = SpacingAroundEqualsRule

    override fun create(rule: Rule): ContextVisitor {
        val enforce = (rule.value as BooleanRuleValue).value
        return SpacingAroundEqualsVisitor(enforce)
    }
}
