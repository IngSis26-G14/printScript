package common.token

import com.ingsis.g14.printscript.common.token.TokenType
import common.source.SourcePosition
import common.source.SourceRange

data class Token(
    val type: TokenType,
    val lexeme: String,
    val range: SourceRange,

    )
