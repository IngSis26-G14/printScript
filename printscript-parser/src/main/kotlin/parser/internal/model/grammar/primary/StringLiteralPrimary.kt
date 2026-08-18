package parser.internal.model.grammar.primary

import common.model.node.Node
import common.model.node.NodeType
import common.model.node.StringLiteralNode
import common.model.token.Token
import common.model.token.TokenType
import common.model.value.StringValue
import common.type.outcome.Outcome
import parser.internal.model.category.MissingStringLiteral
import parser.internal.model.grammar.GrammarFail
import parser.internal.model.grammar.GrammarMatch
import parser.internal.table.GrammarTable

internal class StringLiteralPrimary : Primary {
    override val type: NodeType = StringLiteralNode

    override fun match(
        tokens: List<Token>,
        table: GrammarTable,
    ): Outcome<GrammarMatch, GrammarFail> {
        val first = tokens.getOrElse(0) {
            return Outcome.Error(
                GrammarFail(
                    "Expected string literal",
                    MissingStringLiteral,
                    0,
                ),
            )
        }
        if (first.type != TokenType.STRING_LITERAL) {
            return Outcome.Error(
                GrammarFail(
                    "Expected string literal, got '${first.lexeme}'",
                    MissingStringLiteral,
                    0,
                ),
            )
        }

        val node = buildNode(first)
        return Outcome.Ok(GrammarMatch(node, 1))
    }

    private fun buildNode(first: Token): Node {
        return Node.Leaf(
            value = StringValue(first.lexeme),
            leading = first.leading,
            trailing = first.trailing,
            type = type,
            span = first.span,
        )
    }
}
