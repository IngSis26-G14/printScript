package parser.internal.model.grammar.primary

import common.model.node.BooleanLiteralNode
import common.model.node.Node
import common.model.node.NodeType
import common.model.token.Token
import common.model.token.TokenType
import common.model.value.BooleanValue
import common.type.outcome.Outcome
import parser.internal.model.category.MissingBooleanLiteral
import parser.internal.model.grammar.GrammarFail
import parser.internal.model.grammar.GrammarMatch
import parser.internal.table.GrammarTable

internal class BooleanLiteralPrimary : Primary {
    override val type: NodeType = BooleanLiteralNode

    override fun match(
        tokens: List<Token>,
        table: GrammarTable,
    ): Outcome<GrammarMatch, GrammarFail> {
        val first = tokens.getOrElse(0) {
            return Outcome.Error(
                GrammarFail(
                    "Expected boolean literal",
                    MissingBooleanLiteral,
                    0,
                ),
            )
        }
        if (first.type != TokenType.BOOLEAN_LITERAL) {
            return Outcome.Error(
                GrammarFail(
                    "Expected boolean literal, got '${first.lexeme}'",
                    MissingBooleanLiteral,
                    0,
                ),
            )
        }

        val node = buildNode(first)
        return Outcome.Ok(GrammarMatch(node, 1))
    }

    private fun buildNode(first: Token): Node {
        return Node.Leaf(
            value = BooleanValue(first.lexeme.toBoolean()),
            leading = first.leading,
            trailing = first.trailing,
            type = type,
            span = first.span,
        )
    }
}
