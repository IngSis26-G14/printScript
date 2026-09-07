package linter

import Lexer
import Linter
import Parser
import Validator
import common.error.ErrorFlag
import common.error.onError
import common.io.reader.config.ConfigReader
import common.io.reader.input.InputReader
import common.io.reporter.DiagnosticReporter
import common.type.outcome.Outcome

class LinterRunner(
    private val lexer: Lexer,
    private val parser: Parser,
    private val validator: Validator,
    private val linter: Linter,
) {

    fun run(
        version: String,
        source: InputReader,
        config: ConfigReader,
        reporter: DiagnosticReporter,
    ) {
        val rulesOutcome = config.read()
        val rules = when (rulesOutcome) {
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
        val validations = validator.validate(version, nodes)
            .takeWhile { !flag.hasError }
            .onError(reporter)
        val lints = linter.lint(version, validations, rules)

        for (lint in lints) {
            reporter.report(lint)
        }
    }
}
