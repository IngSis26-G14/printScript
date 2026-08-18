package lexer.scanner

import common.model.span.Span
import common.type.outcome.Outcome
import lexer.SourceCursor
import lexer.categories.Lexical
import lexer.error.LexError
import lexer.scan.TokenScan
import lexer.table.RuleTable

internal class TokenScanner {

    fun scan(cursor: SourceCursor, table: RuleTable): TokenScan {
        if (cursor.isAtEnd()) return TokenScan.Empty

        val rule = table.rules.firstOrNull { it.matches(cursor) }
            ?: return TokenScan.Error(unexpectedCharacter(cursor))

        val previousOffset = cursor.offset

        return when (val outcome = rule.read(cursor)) {
            is Outcome.Ok -> {
                check(cursor.offset > previousOffset) {
                    "${rule::class.simpleName} devolvió un token sin consumir caracteres"
                }
                TokenScan.Ok(outcome.value)
            }
            is Outcome.Error -> TokenScan.Error(outcome.error)
        }
    }

    private fun unexpectedCharacter(cursor: SourceCursor): LexError {
        val position = cursor.currentPosition()
        return LexError(
            message = "Unexpected character '${cursor.peek()}'",
            span = Span(position, position),
            category = Lexical,
        )
    }
}
