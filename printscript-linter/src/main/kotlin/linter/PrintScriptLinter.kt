package linter

import Linter
import common.model.diagnostic.Diagnostic
import common.model.node.Node
import common.model.rule.Rule
import common.model.visitor.VisitorTable
import common.type.outcome.Outcome
import linter.table.VisitorTableRegistry

class PrintScriptLinter : Linter {

    override fun lint(
        version: String,
        nodes: Sequence<Node>,
        rules: Collection<Rule>,
    ): Sequence<Diagnostic> {
        return sequence {
            when (val table = getVisitorTable(version, rules)) {
                is Outcome.Ok -> yieldAll(lintNodes(nodes, table.value))
                is Outcome.Error -> yield(table.error)
            }
        }
    }

    private fun getVisitorTable(
        version: String,
        rules: Collection<Rule>,
    ): Outcome<VisitorTable, Diagnostic> = VisitorTableRegistry.get(version, rules)

    private fun lintNodes(
        nodes: Sequence<Node>,
        table: VisitorTable,
    ): Sequence<Diagnostic> {
        return sequence {
            for (node in nodes) {
                yieldAll(collectErrorsRecursive(node, table))
            }
        }
    }

    private fun collectErrorsRecursive(
        node: Node,
        table: VisitorTable,
    ): Sequence<Diagnostic> {
        return sequence {
            for (visitor in table.visitors) {
                val outcome = node.accept(visitor, table)
                if (outcome is Outcome.Error) {
                    yield(outcome.error)
                }
            }

            if (node is Node.Composite) {
                for (child in node.children) {
                    yieldAll(collectErrorsRecursive(child, table))
                }
            }
        }
    }
}
