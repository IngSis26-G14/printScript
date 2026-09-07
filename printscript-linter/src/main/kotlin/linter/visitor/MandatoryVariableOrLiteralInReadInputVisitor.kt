package linter.visitor

import common.model.diagnostic.Diagnostic
import common.model.diagnostic.severity.Warning
import common.model.node.Node
import common.model.node.ReadInputExpressionNode
import common.model.value.NoneValue
import common.model.value.Value
import common.model.visitor.Visitor
import common.model.visitor.VisitorTable
import common.type.outcome.Outcome
import linter.model.lint.Lint
import linter.model.rule.MandatoryIdentifierOrLiteralInReadInputRule

internal class MandatoryVariableOrLiteralInReadInputVisitor(
    private val enforce: Boolean,
) : Visitor {

    override fun visit(
        node: Node.Leaf,
        table: VisitorTable,
    ): Outcome<Value, Diagnostic> = Outcome.Ok(node.value)

    override fun visit(
        node: Node.Composite,
        table: VisitorTable,
    ): Outcome<Value, Diagnostic> {
        if (!enforce) return Outcome.Ok(NoneValue)

        if (node.type == ReadInputExpressionNode) {
            if (node.children.size >= 3) {
                val argument = node.children.toList()[2]

                if (argument is Node.Composite) {
                    return Outcome.Error(
                        Lint(
                            message = "readInput() must take an identifier or literal argument",
                            span = argument.span,
                            rule = MandatoryIdentifierOrLiteralInReadInputRule,
                            severity = Warning,
                        ),
                    )
                }
            }
        }
        return Outcome.Ok(NoneValue)
    }
}
