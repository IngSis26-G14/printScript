package rules

import SourceCursor
import common.token.Token

internal interface LexerRule {

    fun matches(cursor: SourceCursor): Boolean

    fun read(cursor: SourceCursor): Token
}