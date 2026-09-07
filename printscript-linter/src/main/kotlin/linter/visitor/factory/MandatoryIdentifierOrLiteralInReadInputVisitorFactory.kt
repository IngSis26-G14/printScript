package linter.visitor.factory

import common.model.rule.BooleanRuleValue
import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.Visitor
import linter.model.rule.MandatoryIdentifierOrLiteralInReadInputRule
import linter.table.VisitorFactory
import linter.visitor.MandatoryVariableOrLiteralInReadInputVisitor

internal class MandatoryIdentifierOrLiteralInReadInputVisitorFactory : VisitorFactory {
    override val ruleType: RuleType = MandatoryIdentifierOrLiteralInReadInputRule

    override fun create(rule: Rule): Visitor {
        val enforce = (rule.value as BooleanRuleValue).value
        return MandatoryVariableOrLiteralInReadInputVisitor(enforce)
    }
}
