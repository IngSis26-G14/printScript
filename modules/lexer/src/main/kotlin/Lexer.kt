import common.token.Token
import java.io.Reader

interface Lexer {

    fun tokenize(source: Reader): Sequence<Token>
}