package validator

import Lexer
import Parser
import Validator
import common.error.ErrorFlag
import common.error.onError
import common.io.reader.input.InputReader
import common.io.reporter.DiagnosticReporter
import common.type.outcome.Outcome

class ValidatorRunner(
    private val lexer: Lexer,
    private val parser: Parser,
    private val validator: Validator,
) {
    fun run(
        version: String,
        source: InputReader,
        reporter: DiagnosticReporter,
    ) {
        val flag = ErrorFlag()

        val chars = source.read()
        val tokens = lexer.lex(version, chars)
            .onError(reporter, flag)
        val nodes = parser.parse(version, tokens)
            .takeWhile { !flag.hasError }
            .onError(reporter, flag)
        val validations = validator.validate(version, nodes)
            .takeWhile { !flag.hasError }

        for (validation in validations) {
            if (validation is Outcome.Error) {
                reporter.report(validation.error)
            }
        }
    }
}
