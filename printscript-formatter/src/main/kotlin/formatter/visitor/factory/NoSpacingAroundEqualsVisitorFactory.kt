package formatter.visitor.factory

import common.model.rule.BooleanRuleValue
import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.context.ContextVisitor
import formatter.rule.NoSpacingAroundEqualsRule
import formatter.visitor.NoSpacingAroundEqualsVisitor

internal class NoSpacingAroundEqualsVisitorFactory : ContextVisitorFactory {
    override val ruleType: RuleType = NoSpacingAroundEqualsRule

    override fun create(rule: Rule): ContextVisitor {
        val enforce = (rule.value as BooleanRuleValue).value
        return NoSpacingAroundEqualsVisitor(enforce)
    }
}
