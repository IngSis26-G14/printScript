package lexer

import Lexer
import common.model.diagnostic.Diagnostic
import common.model.token.Token
import common.type.option.Option
import common.type.outcome.Outcome
import lexer.collector.TriviaCollector
import lexer.error.ConfigurationError
import lexer.scan.TokenScan
import lexer.scanner.TokenScanner
import lexer.table.RuleTableRegistry

class PrintScriptLexer internal constructor(
    private val scanner: TokenScanner,
    private val triviaCollector: TriviaCollector,
) : Lexer {

    constructor() : this(TokenScanner(), TriviaCollector())

    override fun lex(version: String, chars: Sequence<Char>): Sequence<Outcome<Token, Diagnostic>> =
        sequence {
            val table = when (val option = RuleTableRegistry.get(version)) {
                is Option.Some -> option.value
                is Option.None -> {
                    yield(Outcome.Error(ConfigurationError("Unsupported PrintScript version: $version")))
                    return@sequence
                }
            }

            val cursor = SourceCursor(chars)

            while (!cursor.isAtEnd()) {
                val leading = triviaCollector.collect(cursor)

                if (cursor.isAtEnd()) break

                when (val scan = scanner.scan(cursor, table)) {
                    is TokenScan.Ok -> yield(Outcome.Ok(scan.token.copy(leading = leading)))
                    is TokenScan.Error -> {
                        yield(Outcome.Error(scan.diagnostic))
                        return@sequence
                    }
                    is TokenScan.Empty -> return@sequence
                }
            }
        }.constrainOnce()
}
