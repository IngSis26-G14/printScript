package lexer

import common.model.diagnostic.Diagnostic
import common.model.token.Token
import common.type.outcome.Outcome

interface Lexer {
    fun lex(version: String, chars: Sequence<Char>): Sequence<Outcome<Token, Diagnostic>>
}