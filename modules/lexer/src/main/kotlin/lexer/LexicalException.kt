package lexer

import common.source.SourcePosition

class LexicalException (
    message: String,
    val position: SourcePosition
    ): RuntimeException(message)

