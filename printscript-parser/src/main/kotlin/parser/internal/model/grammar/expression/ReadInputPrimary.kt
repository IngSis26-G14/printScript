package parser.internal.model.grammar.expression

import common.model.node.LeftParenthesisNode
import common.model.node.Node
import common.model.node.NodeType
import common.model.node.ReadInputExpressionNode
import common.model.node.ReadInputKeywordNode
import common.model.node.RightParenthesisNode
import common.model.span.Span
import common.model.token.Token
import common.model.token.TokenType
import common.model.value.StringValue
import common.type.outcome.Outcome
import common.type.outcome.getOrElse
import parser.internal.model.category.MissingClosingParenthesis
import parser.internal.model.category.MissingExpression
import parser.internal.model.category.MissingOpeningParenthesis
import parser.internal.model.grammar.GrammarFail
import parser.internal.model.grammar.GrammarMatch
import parser.internal.model.grammar.primary.Primary
import parser.internal.table.GrammarTable

internal class ReadInputPrimary : Primary {
    override val type: NodeType = ReadInputExpressionNode

    override fun match(
        tokens: List<Token>,
        table: GrammarTable,
    ): Outcome<GrammarMatch, GrammarFail> {
        var consumed = 0

        val readInput = tokens.getOrElse(consumed) {
            return Outcome.Error(
                GrammarFail(
                    "Expected 'readInput'",
                    MissingExpression,
                    consumed,
                ),
            )
        }
        if (readInput.type != TokenType.READ_INPUT) {
            return Outcome.Error(
                GrammarFail(
                    "Expected 'readInput', got '${readInput.lexeme}'",
                    MissingExpression,
                    consumed,
                ),
            )
        }
        consumed += 1

        val lparen = tokens.getOrElse(consumed) {
            return Outcome.Error(
                GrammarFail(
                    "Expected '(' after 'readInput'",
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

        val innerResult = table.dispatchExpression(tokens.drop(consumed))
        val inner = innerResult.getOrElse {
            return Outcome.Error(
                GrammarFail(
                    it.message,
                    it.category,
                    consumed + it.consumed,
                ),
            )
        }
        consumed += inner.consumed

        val rparen = tokens.getOrElse(consumed) {
            return Outcome.Error(
                GrammarFail(
                    "Expected ')' after expression",
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

        val node = buildNode(readInput, lparen, inner.node, rparen, tokens)
        return Outcome.Ok(GrammarMatch(node, consumed))
    }

    private fun buildNode(
        readInput: Token,
        lparen: Token,
        inner: Node,
        rparen: Token,
        tokens: List<Token>,
    ): Node {
        val children = listOf(
            Node.Leaf(
                ReadInputKeywordNode,
                StringValue(readInput.lexeme),
                readInput.span,
                readInput.leading,
                readInput.trailing,
            ),
            Node.Leaf(
                LeftParenthesisNode,
                StringValue(lparen.lexeme),
                lparen.span,
                lparen.leading,
                lparen.trailing,
            ),
            inner,
            Node.Leaf(
                RightParenthesisNode,
                StringValue(rparen.lexeme),
                rparen.span,
                rparen.leading,
                rparen.trailing,
            ),
        )

        return Node.Composite(
            children = children,
            type = type,
            span = Span(tokens.first().span.start, rparen.span.end),
        )
    }
}
