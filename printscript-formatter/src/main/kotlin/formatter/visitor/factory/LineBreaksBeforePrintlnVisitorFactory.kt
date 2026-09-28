package formatter.visitor.factory

import common.model.rule.IntegerRuleValue
import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.context.ContextVisitor
import formatter.rule.LineBreaksBeforePrintlnRule
import formatter.visitor.LineBreaksBeforePrintlnVisitor

internal class LineBreaksBeforePrintlnVisitorFactory : ContextVisitorFactory {
    override val ruleType: RuleType = LineBreaksBeforePrintlnRule

    override fun create(rule: Rule): ContextVisitor {
        val lineBreaks = (rule.value as IntegerRuleValue).value
        require(lineBreaks in 0..2) { "line-breaks-before-println must be 0, 1, or 2" }
        return LineBreaksBeforePrintlnVisitor(lineBreaks)
    }
}
