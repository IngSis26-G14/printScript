package parser.internal.model.grammar.primary

import common.model.node.Node
import common.model.node.NumberLiteralNode
import common.model.token.Token
import common.model.token.TokenType
import common.model.value.Value
import common.model.value.transformer.FloatValueTransformer
import common.model.value.transformer.IntegerValueTransformer
import common.type.option.Option
import common.type.outcome.Outcome
import parser.internal.model.category.InvalidNumberLiteral
import parser.internal.model.category.MissingNumberLiteral
import parser.internal.model.grammar.GrammarFail
import parser.internal.model.grammar.GrammarMatch
import parser.internal.table.GrammarTable

internal class NumberLiteralPrimary : Primary {
    override val type = NumberLiteralNode

    private val transformers = listOf(
        IntegerValueTransformer,
        FloatValueTransformer,
    )

    override fun match(
        tokens: List<Token>,
        table: GrammarTable,
    ): Outcome<GrammarMatch, GrammarFail> {
        val first = tokens.getOrElse(0) {
            return Outcome.Error(
                GrammarFail(
                    "Expected number literal",
                    MissingNumberLiteral,
                    0,
                ),
            )
        }

        if (first.type != TokenType.NUMBER_LITERAL) {
            return Outcome.Error(
                GrammarFail(
                    "Expected number literal, got '${first.lexeme}'",
                    MissingNumberLiteral,
                    0,
                ),
            )
        }

        val parsedValue = transformValue(first.lexeme)
        return when (parsedValue) {
            is Option.Some -> {
                val node = buildNode(first, parsedValue.value)
                Outcome.Ok(GrammarMatch(node, 1))
            }
            is Option.None -> Outcome.Error(
                GrammarFail(
                    "Invalid number literal: ${first.lexeme}",
                    InvalidNumberLiteral,
                    0,
                ),
            )
        }
    }

    private fun transformValue(lexeme: String): Option<Value> {
        for (transformer in transformers) {
            val result = transformer.parse(lexeme)
            if (result is Option.Some) return result
        }
        return Option.None
    }

    private fun buildNode(
        first: Token,
        value: Value,
    ): Node {
        return Node.Leaf(
            value = value,
            leading = first.leading,
            trailing = first.trailing,
            type = type,
            span = first.span,
        )
    }
}
