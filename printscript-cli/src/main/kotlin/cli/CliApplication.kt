package cli

import Parser
import common.io.reader.input.InputReader
import common.io.reporter.DiagnosticReporter
import interpreter.PrintScriptInterpreter
import lexer.PrintScriptLexer
import linter.LinterRunner
import linter.PrintScriptLinter
import parser.PrintScriptParser
import validator.PrintScriptValidator
import validator.ValidatorRunner
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

class CliApplication(
    private val standardOutput: PrintStream = System.out,
    private val standardError: PrintStream = System.err,
    private val standardInput: BufferedReader = BufferedReader(InputStreamReader(System.`in`)),
    private val environment: Map<String, String> = System.getenv(),
) {
    fun run(args: Array<String>): Int {
        if (args.any { it == "--help" || it == "-h" }) {
            standardOutput.println(ArgumentParser.USAGE)
            return SUCCESS
        }

        val arguments = ArgumentParser.parse(args).getOrElse { exception ->
            standardError.println(exception.message)
            return INVALID_ARGUMENTS
        }

        validateFile(arguments.source, "Source")?.let { message ->
            standardError.println(message)
            return FILE_ERROR
        }
        arguments.config?.let { config ->
            validateFile(config, "Configuration")?.let { message ->
                standardError.println(message)
                return FILE_ERROR
            }
        }

        return try {
            execute(arguments)
        } catch (exception: IOException) {
            standardError.println("CLI failure: ${exception.message}")
            INTERNAL_ERROR
        }
    }

    private fun execute(arguments: CliArguments): Int {
        val source = FileInputReader(arguments.source)
        val reporter = CliDiagnosticReporter(standardError)
        val parser = ProgressParser(
            delegate = PrintScriptParser(),
            totalCharacters = source.characterCount(),
            output = standardError,
        )

        when (arguments.operation) {
            Operation.VALIDATION -> validate(arguments, source, parser, reporter)
            Operation.EXECUTION -> executeSource(arguments, source, parser, reporter)
            Operation.ANALYZING -> analyze(arguments, source, parser, reporter)
            Operation.FORMATTING -> {
                standardError.println(
                    "Formatting is unavailable: this repository has no formatter implementation.",
                )
                return UNAVAILABLE_OPERATION
            }
        }

        return if (reporter.hasDiagnostics) DIAGNOSTIC_FOUND else SUCCESS
    }

    private fun validate(
        arguments: CliArguments,
        source: InputReader,
        parser: Parser,
        reporter: DiagnosticReporter,
    ) {
        ValidatorRunner(
            lexer = PrintScriptLexer(),
            parser = parser,
            validator = PrintScriptValidator(),
        ).run(arguments.version, source, reporter)
    }

    private fun executeSource(
        arguments: CliArguments,
        source: InputReader,
        parser: Parser,
        reporter: CliDiagnosticReporter,
    ) {
        validate(arguments, source, parser, reporter)
        if (reporter.hasDiagnostics) return

        ExecutionRunner(
            lexer = PrintScriptLexer(),
            parser = PrintScriptParser(),
            interpreter = PrintScriptInterpreter(),
        ).run(
            version = arguments.version,
            source = FileInputReader(arguments.source),
            input = StandardInputReader(standardInput),
            output = ConsoleOutputWriter(standardOutput),
            env = SystemEnvReader(environment),
            reporter = reporter,
        )
    }

    private fun analyze(
        arguments: CliArguments,
        source: InputReader,
        parser: Parser,
        reporter: DiagnosticReporter,
    ) {
        LinterRunner(
            lexer = PrintScriptLexer(),
            parser = parser,
            validator = PrintScriptValidator(),
            linter = PrintScriptLinter(),
        ).run(
            version = arguments.version,
            source = source,
            config = JsonConfigReader(checkNotNull(arguments.config)),
            reporter = reporter,
        )
    }

    private fun validateFile(path: Path, description: String): String? = when {
        !Files.exists(path) -> "$description file does not exist: $path"
        !Files.isRegularFile(path) -> "$description path is not a regular file: $path"
        !Files.isReadable(path) -> "$description file is not readable: $path"
        else -> null
    }

    private companion object {
        const val SUCCESS = 0
        const val DIAGNOSTIC_FOUND = 1
        const val INVALID_ARGUMENTS = 2
        const val FILE_ERROR = 3
        const val UNAVAILABLE_OPERATION = 4
        const val INTERNAL_ERROR = 70
    }
}
