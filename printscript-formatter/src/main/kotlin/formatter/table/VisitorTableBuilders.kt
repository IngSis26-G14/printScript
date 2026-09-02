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
import formatter.visitor.factory.MandatorySingleSpaceVisitorFactory
import formatter.visitor.factory.NoSpacingAroundEqualsVisitorFactory
import formatter.visitor.factory.SpacingAfterColonVisitorFactory
import formatter.visitor.factory.SpacingAroundEqualsVisitorFactory
import formatter.visitor.factory.SpacingAroundOperatorVisitorFactory
import formatter.visitor.factory.SpacingBeforeColonVisitorFactory

internal object PrintScriptV10 : ContextVisitorTableBuilder {
    override val factories: Map<String, ContextVisitorFactory> = mapOf(
        NoSpacingAroundEqualsRule.signature to NoSpacingAroundEqualsVisitorFactory(),
        SpacingAroundEqualsRule.signature to SpacingAroundEqualsVisitorFactory(),
        SpacingBeforeColonRule.signature to SpacingBeforeColonVisitorFactory(),
        SpacingAfterColonRule.signature to SpacingAfterColonVisitorFactory(),
        MandatorySingleSpaceRule.signature to MandatorySingleSpaceVisitorFactory(),
        LineBreaksAfterPrintlnRule.signature to LineBreaksAfterPrintlnVisitorFactory(),
        SpacingAroundOperatorRule.signature to SpacingAroundOperatorVisitorFactory(
            listOf(PlusNode, MinusNode, MultiplyNode, DivideNode),
        ),
        LineBreakAfterStatementRule.signature to LineBreakAfterStatementVisitorFactory(),
    )
}

internal object PrintScriptV11 : ContextVisitorTableBuilder {
    override val factories: Map<String, ContextVisitorFactory> = PrintScriptV10.factories + mapOf(
        IndentsInsideIfBlockRule.signature to IndentsInsideIfVisitorFactory(),
        IfBraceSameLineRule.signature to IfBraceSameLineVisitorFactory(),
        IfBraceBelowLineRule.signature to IfBraceBelowLineVisitorFactory(),
    )
}
