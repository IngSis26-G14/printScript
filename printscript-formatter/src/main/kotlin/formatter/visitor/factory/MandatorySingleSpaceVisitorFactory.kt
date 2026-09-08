package formatter.visitor.factory

import common.model.rule.BooleanRuleValue
import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.context.ContextVisitor
import formatter.rule.MandatorySingleSpaceRule
import formatter.visitor.MandatorySingleSpaceVisitor

internal class MandatorySingleSpaceVisitorFactory : ContextVisitorFactory {
    override val ruleType: RuleType = MandatorySingleSpaceRule

    override fun create(rule: Rule): ContextVisitor {
        val enforce = (rule.value as BooleanRuleValue).value
        return MandatorySingleSpaceVisitor(enforce)
    }
}
