package parser.internal.table

import common.model.token.Token
import common.type.option.Option
import common.type.option.getOrElse
import common.type.option.map
import common.type.option.maxBy
import common.type.outcome.Outcome
import parser.internal.model.category.InvalidExpression
import parser.internal.model.category.InvalidStatement
import parser.internal.model.category.MissingExpression
import parser.internal.model.category.MissingStatement
import parser.internal.model.category.System
import parser.internal.model.grammar.Grammar
import parser.internal.model.grammar.GrammarFail
import parser.internal.model.grammar.GrammarMatch
import parser.internal.model.grammar.expression.Expression
import parser.internal.model.grammar.primary.Primary
import parser.internal.model.grammar.statement.Statement

internal interface GrammarTable {
    val statements: Collection<Statement>
    val expressions: Collection<Expression>
    val primaries: Collection<Primary>

    fun dispatchStatement(tokens: List<Token>): Outcome<GrammarMatch, GrammarFail> {
        return when (val result = dispatch(tokens, statements)) {
            is Outcome.Ok -> result
            is Outcome.Error -> {
                if (result.error.consumed == 0) {
                    val message = tokens.getOrElse(0) {
                        val fail = GrammarFail(
                            "Expected statement",
                            MissingStatement,
                            0,
                        )
                        return Outcome.Error(fail)
                    }.let { "Expected statement, got '${it.lexeme}'" }
                    Outcome.Error(
                        GrammarFail(
                            message,
                            InvalidStatement,
                            0,
                        ),
                    )
                } else {
                    result
                }
            }
        }
    }

    fun dispatchExpression(tokens: List<Token>): Outcome<GrammarMatch, GrammarFail> {
        return when (val result = dispatch(tokens, expressions)) {
            is Outcome.Ok -> result
            is Outcome.Error -> {
                if (result.error.consumed == 0) {
                    val message = tokens.getOrElse(0) {
                        val fail = GrammarFail(
                            "Expected expression",
                            MissingExpression,
                            0,
                        )
                        return Outcome.Error(fail)
                    }.let { "Expected expression, got '${it.lexeme}'" }
                    Outcome.Error(
                        GrammarFail(
                            message,
                            InvalidExpression,
                            0,
                        ),
                    )
                } else {
                    result
                }
            }
        }
    }

    fun dispatchPrimary(tokens: List<Token>): Outcome<GrammarMatch, GrammarFail> {
        return when (val result = dispatch(tokens, primaries)) {
            is Outcome.Ok -> result
            is Outcome.Error -> {
                if (result.error.consumed == 0) {
                    val message = tokens.getOrElse(0) {
                        val fail = GrammarFail(
                            "Expected expression",
                            MissingExpression,
                            0,
                        )
                        return Outcome.Error(fail)
                    }.let { "Expected expression, got '${it.lexeme}'" }
                    Outcome.Error(
                        GrammarFail(
                            message,
                            InvalidExpression,
                            0,
                        ),
                    )
                } else {
                    result
                }
            }
        }
    }

    private fun dispatch(
        tokens: List<Token>,
        grammars: Collection<Grammar>,
    ): Outcome<GrammarMatch, GrammarFail> {
        var bestMatch: Option<GrammarMatch> = Option.None
        var lastFail: Option<GrammarFail> = Option.None

        for (grammar in grammars) {
            when (val outcome = grammar.match(tokens, this)) {
                is Outcome.Ok -> {
                    bestMatch = bestMatch.maxBy({ it.consumed }, outcome.value)
                }
                is Outcome.Error -> {
                    lastFail = lastFail.maxBy({ it.consumed }, outcome.error)
                }
            }
        }

        val fail = lastFail.getOrElse {
            GrammarFail(
                "Internal parser error",
                System,
                0,
            )
        }
        return bestMatch.map { Outcome.Ok(it) }.getOrElse { Outcome.Error(fail) }
    }
}
