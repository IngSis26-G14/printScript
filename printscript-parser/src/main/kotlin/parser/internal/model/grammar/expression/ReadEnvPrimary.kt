package parser.internal.model.grammar.expression

import common.model.node.LeftParenthesisNode
import common.model.node.Node
import common.model.node.NodeType
import common.model.node.ReadEnvExpressionNode
import common.model.node.ReadEnvKeywordNode
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

internal class ReadEnvPrimary : Primary {
    override val type: NodeType = ReadEnvExpressionNode

    override fun match(
        tokens: List<Token>,
        table: GrammarTable,
    ): Outcome<GrammarMatch, GrammarFail> {
        var consumed = 0

        val readEnv = tokens.getOrElse(consumed) {
            return Outcome.Error(
                GrammarFail(
                    "Expected 'readEnv'",
                    MissingExpression,
                    consumed,
                ),
            )
        }
        if (readEnv.type != TokenType.READ_ENV) {
            return Outcome.Error(
                GrammarFail(
                    "Expected 'readEnv', got '${readEnv.lexeme}'",
                    MissingExpression,
                    consumed,
                ),
            )
        }
        consumed += 1

        val lparen = tokens.getOrElse(consumed) {
            return Outcome.Error(
                GrammarFail(
                    "Expected '(' after 'readEnv'",
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

        val node = buildNode(readEnv, lparen, inner.node, rparen, tokens)
        return Outcome.Ok(GrammarMatch(node, consumed))
    }

    private fun buildNode(
        readEnv: Token,
        lparen: Token,
        inner: Node,
        rparen: Token,
        tokens: List<Token>,
    ): Node {
        val children = listOf(
            Node.Leaf(
                ReadEnvKeywordNode,
                StringValue(readEnv.lexeme),
                readEnv.span,
                readEnv.leading,
                readEnv.trailing,
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
