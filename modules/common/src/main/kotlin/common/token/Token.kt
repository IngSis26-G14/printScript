package common.token

import com.ingsis.g14.printscript.common.token.TokenType
import common.source.SourcePosition

data class Token(
    val type: TokenType,
    val lexeme: String,
    val position: SourcePosition,

    )
