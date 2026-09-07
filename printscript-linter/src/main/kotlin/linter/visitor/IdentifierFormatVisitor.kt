package linter.visitor

import common.model.diagnostic.Diagnostic
import common.model.diagnostic.severity.Warning
import common.model.node.IdentifierNode
import common.model.node.Node
import common.model.value.NoneValue
import common.model.value.Value
import common.model.visitor.Visitor
import common.model.visitor.VisitorTable
import common.type.outcome.Outcome
import linter.model.lint.Lint
import linter.model.rule.IdentifierFormatRule

internal class IdentifierFormatVisitor(
    private val formatName: String,
    private val regex: Regex,
) : Visitor {

    override fun visit(
        node: Node.Leaf,
        table: VisitorTable,
    ): Outcome<Value, Diagnostic> {
        if (node.type != IdentifierNode) {
            return Outcome.Ok(node.value)
        }

        val identifier = node.value.format()
        val isValid = regex.matches(identifier)

        return if (isValid) {
            Outcome.Ok(node.value)
        } else {
            Outcome.Error(
                Lint(
                    message = "Identifier '$identifier' does not follow $formatName format",
                    span = node.span,
                    rule = IdentifierFormatRule,
                    severity = Warning,
                ),
            )
        }
    }

    override fun visit(
        node: Node.Composite,
        table: VisitorTable,
    ): Outcome<Value, Diagnostic> = Outcome.Ok(NoneValue)
}
