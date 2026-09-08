package formatter.visitor.factory

import common.model.rule.IntegerRuleValue
import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.context.ContextVisitor
import formatter.rule.LineBreaksAfterPrintlnRule
import formatter.visitor.LineBreaksAfterPrintlnVisitor

internal class LineBreaksAfterPrintlnVisitorFactory : ContextVisitorFactory {
    override val ruleType: RuleType = LineBreaksAfterPrintlnRule

    override fun create(rule: Rule): ContextVisitor {
        val lineBreaks = (rule.value as IntegerRuleValue).value
        return LineBreaksAfterPrintlnVisitor(lineBreaks)
    }
}
