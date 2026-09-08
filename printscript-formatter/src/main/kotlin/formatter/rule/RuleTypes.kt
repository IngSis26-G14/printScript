package formatter.rule

import common.model.rule.RuleType

data object SpacingAroundEqualsRule : RuleType {
    override val signature = "enforce-spacing-around-equals"
    override val name = "SpacingAroundEquals"
}

data object NoSpacingAroundEqualsRule : RuleType {
    override val signature = "enforce-no-spacing-around-equals"
    override val name = "NoSpacingAroundEquals"
}

data object SpacingAfterColonRule : RuleType {
    override val signature = "enforce-spacing-after-colon-in-declaration"
    override val name = "SpacingAfterColon"
}

data object SpacingBeforeColonRule : RuleType {
    override val signature = "enforce-spacing-before-colon-in-declaration"
    override val name = "SpacingBeforeColon"
}

data object MandatorySingleSpaceRule : RuleType {
    override val signature = "mandatory-single-space-separation"
    override val name = "MandatorySingleSpace"
}

data object SpacingAroundOperatorRule : RuleType {
    override val signature = "mandatory-space-surrounding-operations"
    override val name = "SpacingAroundOperator"
}

data object LineBreakAfterStatementRule : RuleType {
    override val signature = "mandatory-line-break-after-statement"
    override val name = "LineBreakAfterStatement"
}

data object LineBreaksAfterPrintlnRule : RuleType {
    override val signature = "line-breaks-after-println"
    override val name = "LineBreakAfterPrintln"
}

data object IndentsInsideIfBlockRule : RuleType {
    override val signature = "indent-inside-if"
    override val name = "IndentsInsideIfBlock"
}

data object IfBraceBelowLineRule : RuleType {
    override val signature = "if-brace-below-line"
    override val name = "IfBraceBelowLine"
}

data object IfBraceSameLineRule : RuleType {
    override val signature = "if-brace-same-line"
    override val name = "IfBraceInSameLine"
}
