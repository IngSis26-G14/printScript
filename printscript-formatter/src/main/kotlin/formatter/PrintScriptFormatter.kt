package formatter

import Formatter
import common.model.diagnostic.Diagnostic
import common.model.doc.Doc
import common.model.node.IfStatementNode
import common.model.node.Node
import common.model.node.PrintlnStatementNode
import common.model.node.SemicolonNode
import common.model.rule.BooleanRuleValue
import common.model.rule.IntegerRuleValue
import common.model.rule.Rule
import common.model.trivia.NewlineTrivia
import common.model.trivia.Trivia
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome
import formatter.manipulator.TriviaManipulator
import formatter.model.value.NodeValue
import formatter.rule.LineBreakAfterStatementRule
import formatter.rule.LineBreaksAfterPrintlnRule
import formatter.table.VisitorTableRegistry
import formatter.traversal.NodeTraversal
import formatter.type.toDoc

class PrintScriptFormatter : Formatter {

    override fun format(
        version: String,
        nodes: Sequence<Node>,
        rules: Collection<Rule>,
    ): Sequence<Outcome<Doc, Diagnostic>> {
        return sequence {
            when (val table = getVisitorTable(version, rules)) {
                is Outcome.Ok -> {
                    yieldAll(formatNodes(nodes, table.value, rules))
                }
                is Outcome.Error -> {
                    yield(Outcome.Error(table.error))
                }
            }
        }
    }

    private fun getVisitorTable(
        version: String,
        rules: Collection<Rule>,
    ): Outcome<ContextVisitorTable, Diagnostic> = VisitorTableRegistry.get(version, rules)

    private fun formatNodes(
        nodes: Sequence<Node>,
        table: ContextVisitorTable,
        rules: Collection<Rule>,
    ): Sequence<Outcome<Doc, Diagnostic>> {
        return sequence {
            var currentContext = VisitorContext()
            var previous: Node? = null
            val afterPrintln = (rules.firstOrNull { it.signature == LineBreaksAfterPrintlnRule.signature }?.value as? IntegerRuleValue)?.value
            val afterStatement = rules.any {
                it.signature == LineBreakAfterStatementRule.signature && it.value == BooleanRuleValue(true)
            }

            for (node in nodes) {
                val visitResult = table.dispatch(node, currentContext)
                currentContext = visitResult.context

                when (val outcome = visitResult.outcome) {
                    is Outcome.Ok -> {
                        var current = (outcome.value as? NodeValue)?.value ?: node
                        previous?.let { prior ->
                            val newlineCount = separatorAfter(prior, afterPrintln, afterStatement)
                            if (newlineCount != null) {
                                current = TriviaManipulator.removeLeading(current, NewlineTrivia)
                                val cleaned = TriviaManipulator.removeTrailing(prior, NewlineTrivia)
                                val newlines = List(newlineCount) { Trivia(NewlineTrivia, "\n", prior.span) }
                                yield(Outcome.Ok(TriviaManipulator.addTrailing(cleaned, newlines).toDoc()))
                            } else {
                                yield(Outcome.Ok(prior.toDoc()))
                            }
                        }
                        previous = current
                    }
                    is Outcome.Error -> {
                        previous?.let { yield(Outcome.Ok(it.toDoc())) }
                        previous = null
                        yield(outcome)
                    }
                }
            }
            previous?.let { last ->
                val finalNode = if (separatorAfter(last, afterPrintln, afterStatement) != null) {
                    TriviaManipulator.removeTrailing(last, NewlineTrivia)
                } else {
                    last
                }
                yield(Outcome.Ok(finalNode.toDoc()))
            }
        }
    }

    private fun separatorAfter(node: Node, afterPrintln: Int?, afterStatement: Boolean): Int? = when {
        afterPrintln != null && node.type == PrintlnStatementNode -> afterPrintln + 1
        afterStatement && (NodeTraversal.endsWith(node, SemicolonNode) || node.type == IfStatementNode) -> 1
        else -> null
    }
}
