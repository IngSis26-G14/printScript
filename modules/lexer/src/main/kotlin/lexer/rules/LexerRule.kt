package lexer.rules

import lexer.SourceCursor
import common.model.token.Token
import common.model.diagnostic.Diagnostic
import common.type.outcome.Outcome

internal interface LexerRule {

    fun matches(cursor: SourceCursor): Boolean

    fun read(cursor: SourceCursor): Outcome<Token, Diagnostic>
}