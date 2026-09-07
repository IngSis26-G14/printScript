package cli

import Interpreter
import Lexer
import Parser
import common.error.ErrorFlag
import common.error.onError
import common.io.reader.env.EnvReader
import common.io.reader.input.InputReader
import common.io.reporter.DiagnosticReporter
import common.io.writer.OutputWriter

class ExecutionRunner(
    private val lexer: Lexer,
    private val parser: Parser,
    private val interpreter: Interpreter,
) {
    fun run(
        version: String,
        source: InputReader,
        input: InputReader,
        output: OutputWriter,
        env: EnvReader,
        reporter: DiagnosticReporter,
    ) {
        val flag = ErrorFlag()
        val tokens = lexer.lex(version, source.read()).onError(reporter, flag)
        val nodes = parser.parse(version, tokens)
            .takeWhile { !flag.hasError }
            .onError(reporter, flag)

        val diagnostics = interpreter.interpret(
            version = version,
            nodes = nodes.takeWhile { !flag.hasError },
            input = input,
            output = output,
            env = env,
        )

        diagnostics.forEach(reporter::report)
    }
}
