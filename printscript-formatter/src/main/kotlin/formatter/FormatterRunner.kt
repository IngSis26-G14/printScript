package formatter

import Formatter
import Lexer
import Parser
import common.error.ErrorFlag
import common.error.onError
import common.io.reader.config.ConfigReader
import common.io.reader.input.InputReader
import common.io.reporter.DiagnosticReporter
import common.io.writer.OutputWriter
import common.type.outcome.Outcome

class FormatterRunner(
    private val lexer: Lexer,
    private val parser: Parser,
    private val formatter: Formatter,
) {
    fun run(
        version: String,
        source: InputReader,
        target: OutputWriter,
        config: ConfigReader,
        reporter: DiagnosticReporter,
    ) {
        val rules = when (val rulesOutcome = config.read()) {
            is Outcome.Ok -> rulesOutcome.value
            is Outcome.Error -> {
                reporter.report(rulesOutcome.error)
                return
            }
        }

        val flag = ErrorFlag()

        val chars = source.read()
        val tokens = lexer.lex(version, chars)
            .onError(reporter, flag)
        val nodes = parser.parse(version, tokens)
            .takeWhile { !flag.hasError }
            .onError(reporter, flag)

        val docs = formatter.format(version, nodes, rules)

        val formattedSequence = sequence {
            for (doc in docs) {
                when (doc) {
                    is Outcome.Ok -> {
                        val formatted = doc.value.format()
                        yield(formatted)
                    }
                    is Outcome.Error -> {
                        reporter.report(doc.error)
                        return@sequence
                    }
                }
            }
        }
        target.write(formattedSequence)
    }
}
