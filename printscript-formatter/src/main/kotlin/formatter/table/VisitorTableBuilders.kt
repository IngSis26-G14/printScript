package formatter.table

import common.model.node.DivideNode
import common.model.node.MinusNode
import common.model.node.MultiplyNode
import common.model.node.PlusNode
import formatter.rule.IfBraceBelowLineRule
import formatter.rule.IfBraceSameLineRule
import formatter.rule.IndentsInsideIfBlockRule
import formatter.rule.LineBreakAfterStatementRule
import formatter.rule.LineBreaksAfterPrintlnRule
import formatter.rule.LineBreaksBeforePrintlnRule
import formatter.rule.MandatorySingleSpaceRule
import formatter.rule.NoSpacingAroundEqualsRule
import formatter.rule.SpacingAfterColonRule
import formatter.rule.SpacingAroundEqualsRule
import formatter.rule.SpacingAroundOperatorRule
import formatter.rule.SpacingBeforeColonRule
import formatter.visitor.factory.ContextVisitorFactory
import formatter.visitor.factory.IfBraceBelowLineVisitorFactory
import formatter.visitor.factory.IfBraceSameLineVisitorFactory
import formatter.visitor.factory.IndentsInsideIfVisitorFactory
import formatter.visitor.factory.LineBreakAfterStatementVisitorFactory
import formatter.visitor.factory.LineBreaksAfterPrintlnVisitorFactory
import formatter.visitor.factory.LineBreaksBeforePrintlnVisitorFactory
import formatter.visitor.factory.MandatorySingleSpaceVisitorFactory
import formatter.visitor.factory.NoSpacingAroundEqualsVisitorFactory
import formatter.visitor.factory.SpacingAfterColonVisitorFactory
import formatter.visitor.factory.SpacingAroundEqualsVisitorFactory
import formatter.visitor.factory.SpacingAroundOperatorVisitorFactory
import formatter.visitor.factory.SpacingBeforeColonVisitorFactory

internal object PrintScriptV10 : ContextVisitorTableBuilder {
    override val factories: Map<String, ContextVisitorFactory> = mapOf(
        NoSpacingAroundEqualsRule.signature to NoSpacingAroundEqualsVisitorFactory(),
        MandatorySingleSpaceRule.signature to MandatorySingleSpaceVisitorFactory(),
        SpacingBeforeColonRule.signature to SpacingBeforeColonVisitorFactory(),
        SpacingAfterColonRule.signature to SpacingAfterColonVisitorFactory(),
        SpacingAroundEqualsRule.signature to SpacingAroundEqualsVisitorFactory(),
        SpacingAroundOperatorRule.signature to SpacingAroundOperatorVisitorFactory(
            listOf(PlusNode, MinusNode, MultiplyNode, DivideNode),
        ),
        LineBreakAfterStatementRule.signature to LineBreakAfterStatementVisitorFactory(),
        LineBreaksAfterPrintlnRule.signature to LineBreaksAfterPrintlnVisitorFactory(),
        LineBreaksBeforePrintlnRule.signature to LineBreaksBeforePrintlnVisitorFactory(),
    )
    override val mandatory = setOf(
        MandatorySingleSpaceRule.signature,
        SpacingAroundOperatorRule.signature,
        LineBreakAfterStatementRule.signature,
    )
}

internal object PrintScriptV11 : ContextVisitorTableBuilder {
    override val factories = PrintScriptV10.factories + mapOf(
        IfBraceSameLineRule.signature to IfBraceSameLineVisitorFactory(),
        IfBraceBelowLineRule.signature to IfBraceBelowLineVisitorFactory(),
        IndentsInsideIfBlockRule.signature to IndentsInsideIfVisitorFactory(),
    )
    override val mandatory = PrintScriptV10.mandatory + IfBraceSameLineRule.signature
}
