package linter.model.rule

import common.model.rule.RuleType

data object IdentifierFormatRule : RuleType {
    override val signature = "identifier_format"
    override val name = "IdentifierFormat"
}

data object MandatoryIdentifierOrLiteralInPrintlnRule : RuleType {
    override val signature = "mandatory-variable-or-literal-in-println"
    override val name = "MandatoryIdentifierOrLiteralInPrintln"
}

data object MandatoryIdentifierOrLiteralInReadInputRule : RuleType {
    override val signature = "mandatory-variable-or-literal-in-readInput"
    override val name = "MandatoryIdentifierOrLiteralInReadInput"
}
