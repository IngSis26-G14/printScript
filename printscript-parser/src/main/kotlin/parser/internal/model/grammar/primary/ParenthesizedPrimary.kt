package parser.internal.model.grammar.primary

import common.model.node.LeftParenthesisNode
import common.model.node.Node
import common.model.node.ParenthesizedExpressionNode
import common.model.node.RightParenthesisNode
import common.model.span.Span
import common.model.token.Token
import common.model.token.TokenType
import common.model.value.StringValue
import common.type.outcome.Outcome
import common.type.outcome.getOrElse
import parser.internal.model.category.MissingClosingParenthesis
import parser.internal.model.category.MissingOpeningParenthesis
import parser.internal.model.grammar.GrammarFail
import parser.internal.model.grammar.GrammarMatch
import parser.internal.table.GrammarTable

internal class ParenthesizedPrimary : Primary {
    override val type = ParenthesizedExpressionNode

    override fun match(
        tokens: List<Token>,
        table: GrammarTable,
    ): Outcome<GrammarMatch, GrammarFail> {
        var consumed = 0

        val lparen = tokens.getOrElse(consumed) {
            return Outcome.Error(
                GrammarFail(
                    "Expected '('",
                    MissingOpeningParenthesis,
                    consumed,
                ),
            )
        }
        if (lparen.type != TokenType.LEFT_PARENTHESIS) {
            return Outcome.Error(
                GrammarFail(
                    "Expected '(', got '${lparen.lexeme}'",
                    MissingOpeningParenthesis,
                    consumed,
                ),
            )
        }
        consumed += 1

        val inner = table.dispatchExpression(tokens.drop(consumed)).getOrElse {
            return Outcome.Error(it)
        }
        consumed += inner.consumed

        val rparen = tokens.getOrElse(consumed) {
            return Outcome.Error(
                GrammarFail(
                    "Expected ')'",
                    MissingClosingParenthesis,
                    consumed,
                ),
            )
        }
        if (rparen.type != TokenType.RIGHT_PARENTHESIS) {
            return Outcome.Error(
                GrammarFail(
                    "Expected ')', got '${rparen.lexeme}'",
                    MissingClosingParenthesis,
                    consumed,
                ),
            )
        }
        consumed += 1

        val node = buildNode(lparen, inner, rparen)
        return Outcome.Ok(GrammarMatch(node, consumed))
    }

    private fun buildNode(
        lparen: Token,
        inner: GrammarMatch,
        rparen: Token,
    ): Node {
        return Node.Composite(
            children = listOf(
                Node.Leaf(
                    LeftParenthesisNode,
                    StringValue(lparen.lexeme),
                    lparen.span,
                    lparen.leading,
                    lparen.trailing,
                ),
                inner.node,
                Node.Leaf(
                    RightParenthesisNode,
                    StringValue(rparen.lexeme),
                    rparen.span,
                    rparen.leading,
                    rparen.trailing,
                ),
            ),
            type = type,
            span = Span(lparen.span.start, rparen.span.end),
        )
    }
}
