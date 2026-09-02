package formatter.visitor.factory

import common.model.rule.BooleanRuleValue
import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.context.ContextVisitor
import formatter.rule.IfBraceSameLineRule
import formatter.visitor.IfBraceSameLineVisitor

internal class IfBraceSameLineVisitorFactory : ContextVisitorFactory {
    override val ruleType: RuleType = IfBraceSameLineRule

    override fun create(rule: Rule): ContextVisitor {
        val enforce = (rule.value as BooleanRuleValue).value
        return IfBraceSameLineVisitor(enforce)
    }
}
