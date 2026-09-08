package formatter.visitor.factory

import common.model.rule.IntegerRuleValue
import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.context.ContextVisitor
import formatter.rule.LineBreakAfterStatementRule
import formatter.visitor.IndentsInsideIfVisitor

internal class IndentsInsideIfVisitorFactory : ContextVisitorFactory {
    override val ruleType: RuleType = LineBreakAfterStatementRule

    override fun create(rule: Rule): ContextVisitor {
        val indents = (rule.value as IntegerRuleValue).value
        return IndentsInsideIfVisitor(indents)
    }
}
