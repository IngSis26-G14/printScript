package formatter.visitor.factory

import common.model.rule.BooleanRuleValue
import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.context.ContextVisitor
import formatter.rule.LineBreakAfterStatementRule
import formatter.visitor.LineBreakAfterStatementVisitor

internal class LineBreakAfterStatementVisitorFactory : ContextVisitorFactory {
    override val ruleType: RuleType = LineBreakAfterStatementRule

    override fun create(rule: Rule): ContextVisitor {
        val enforce = (rule.value as BooleanRuleValue).value
        return LineBreakAfterStatementVisitor(enforce)
    }
}
