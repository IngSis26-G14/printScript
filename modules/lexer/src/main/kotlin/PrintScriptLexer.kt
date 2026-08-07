import com.ingsis.g14.printscript.common.token.TokenType
import common.token.Token
import java.io.Reader

class PrintScriptLexer: Lexer {


    private val singleCharacterToken = mapOf(
        '(' to TokenType.LEFT_PARENTHESIS,
        ')' to TokenType.RIGHT_PARENTHESIS,
        '=' to TokenType.ASSIGNMENT_OPERATOR,
        '-' to TokenType.SUBTRACTION_OPERATOR,
        '+' to TokenType.ADDITION_OPERATOR,
        '/' to TokenType.DIVISION_OPERATOR,
        '*' to TokenType.MULTIPLICATION_OPERATOR,
        ';' to TokenType.SEMICOLON,
        ':' to TokenType.COLON,
    )

    override fun tokenize(source: Reader): Sequence<Token> {
        // a completar
        return emptySequence()
    }

}