package common.token

import common.source.SourceRange

data class Token(
    val type: TokenType,
    val lexeme: String,
    val range: SourceRange,

    ) {
    constructor(type: TokenType, lexeme: String, range: Any) : this(type, lexeme, range)
}
