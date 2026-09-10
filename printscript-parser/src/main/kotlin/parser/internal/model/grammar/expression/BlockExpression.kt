package parser.internal.model.grammar.expression

import common.model.node.BlockNode
import common.model.node.LeftBraceNode
import common.model.node.Node
import common.model.node.RightBraceNode
import common.model.span.Span
import common.model.token.Token
import common.model.token.TokenType
import common.model.value.StringValue
import common.type.outcome.Outcome
import common.type.outcome.getOrElse
import parser.internal.model.category.MissingClosingBrace
import parser.internal.model.category.MissingOpeningBrace
import parser.internal.model.grammar.GrammarFail
import parser.internal.model.grammar.GrammarMatch
import parser.internal.table.GrammarTable

internal class BlockExpression : Expression {
    override val type = BlockNode

    override fun match(
        tokens: List<Token>,
        table: GrammarTable,
    ): Outcome<GrammarMatch, GrammarFail> {
        var consumed = 0

        val lbrace = tokens.getOrElse(consumed) {
            return Outcome.Error(
                GrammarFail(
                    "Expected '{'",
                    MissingOpeningBrace,
                    consumed,
                ),
            )
        }
        if (lbrace.type != TokenType.LEFT_BRACE) {
            return Outcome.Error(
                GrammarFail(
                    "Expected '{', got '${lbrace.lexeme}'",
                    MissingOpeningBrace,
                    consumed,
                ),
            )
        }
        consumed += 1

        val statements = mutableListOf<Node>()
        while (consumed < tokens.size && tokens[consumed].type != TokenType.RIGHT_BRACE) {
            val stmt = table.dispatchStatement(tokens.subList(consumed, tokens.size))
                .getOrElse {
                    return Outcome.Error(
                        GrammarFail(
                            it.message,
                            it.category,
                            consumed + it.consumed,
                        ),
                    )
                }
            statements.add(stmt.node)
            consumed += stmt.consumed
        }

        val rbrace = tokens
            .getOrElse(consumed) {
                return Outcome.Error(
                    GrammarFail(
                        "Expected '}'",
                        MissingClosingBrace,
                        consumed,
                    ),
                )
            }
        if (rbrace.type != TokenType.RIGHT_BRACE) {
            return Outcome.Error(
                GrammarFail(
                    "Expected '}', got '${rbrace.lexeme}'",
                    MissingClosingBrace,
                    consumed,
                ),
            )
        }
        consumed += 1

        val node = buildNode(lbrace, statements, rbrace)
        return Outcome.Ok(GrammarMatch(node, consumed))
    }

    private fun buildNode(
        lbrace: Token,
        statements: MutableList<Node>,
        rbrace: Token,
    ): Node {
        return Node.Composite(
            children = buildList {
                add(
                    Node.Leaf(
                        LeftBraceNode,
                        StringValue(lbrace.lexeme),
                        lbrace.span,
                        lbrace.leading,
                        lbrace.trailing,
                    ),
                )
                addAll(statements)
                add(
                    Node.Leaf(
                        RightBraceNode,
                        StringValue(rbrace.lexeme),
                        rbrace.span,
                        rbrace.leading,
                        rbrace.trailing,
                    ),
                )
            },
            type = type,
            span = Span(lbrace.span.start, rbrace.span.end),
        )
    }
}
