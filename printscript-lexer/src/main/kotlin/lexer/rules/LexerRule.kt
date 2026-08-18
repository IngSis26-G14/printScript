package lexer.rules

import common.model.diagnostic.Diagnostic
import common.model.token.Token
import common.type.outcome.Outcome
import lexer.SourceCursor

internal interface LexerRule {

    fun matches(cursor: SourceCursor): Boolean

    fun read(cursor: SourceCursor): Outcome<Token, Diagnostic>
}
