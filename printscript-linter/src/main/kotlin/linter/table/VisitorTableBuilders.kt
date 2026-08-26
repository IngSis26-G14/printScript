package linter.table

import linter.model.rule.IdentifierFormatRule
import linter.model.rule.MandatoryIdentifierOrLiteralInPrintlnRule
import linter.model.rule.MandatoryIdentifierOrLiteralInReadInputRule
import linter.visitor.factory.IdentifierFormatVisitorFactory
import linter.visitor.factory.MandatoryIdentifierOrLiteralInPrintlnRuleFactory
import linter.visitor.factory.MandatoryIdentifierOrLiteralInReadInputVisitorFactory

internal object PrintScriptV10 : VisitorTableBuilder {
    override val factories: Map<String, VisitorFactory> = mapOf(
        IdentifierFormatRule.signature to IdentifierFormatVisitorFactory(),
        MandatoryIdentifierOrLiteralInPrintlnRule.signature to
            MandatoryIdentifierOrLiteralInPrintlnRuleFactory(),
    )
}

internal object PrintScriptV11 : VisitorTableBuilder {
    override val factories: Map<String, VisitorFactory> = PrintScriptV10.factories + mapOf(
        MandatoryIdentifierOrLiteralInReadInputRule.signature to
            MandatoryIdentifierOrLiteralInReadInputVisitorFactory(),
    )
}
