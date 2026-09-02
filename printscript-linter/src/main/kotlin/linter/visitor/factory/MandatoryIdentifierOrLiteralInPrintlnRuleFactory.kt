package linter.visitor.factory

import common.model.rule.BooleanRuleValue
import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.Visitor
import linter.model.rule.MandatoryIdentifierOrLiteralInPrintlnRule
import linter.table.VisitorFactory
import linter.visitor.MandatoryVariableOrLiteralInPrintlnVisitor

internal class MandatoryIdentifierOrLiteralInPrintlnRuleFactory : VisitorFactory {
    override val ruleType: RuleType = MandatoryIdentifierOrLiteralInPrintlnRule

    override fun create(rule: Rule): Visitor {
        val enforce = (rule.value as BooleanRuleValue).value
        return MandatoryVariableOrLiteralInPrintlnVisitor(enforce)
    }
}
