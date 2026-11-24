package editor

import cli.JsonConfigReader
import common.io.reader.config.ConfigReader
import common.io.reader.input.InputReader
import common.io.reporter.DiagnosticReporter
import common.io.writer.OutputWriter
import common.model.diagnostic.Diagnostic
import common.model.rule.BooleanRuleValue
import common.model.rule.Rule
import common.model.rule.StringRuleValue
import common.type.outcome.Outcome
import formatter.FormatterRunner
import formatter.PrintScriptFormatter
import lexer.PrintScriptLexer
import linter.LinterRunner
import linter.PrintScriptLinter
import parser.PrintScriptParser
import validator.PrintScriptValidator
import java.nio.file.Path

/** Editor-independent adapter. Never executes source or reads runtime input. */
class EditorService(private val checkCanceled: Runnable = Runnable {}) {
    fun analyze(text: String, version: String, lintConfig: Path?, formatConfig: Path?): EditorResult {
        val issues = mutableListOf<EditorIssue>()
        LinterRunner(PrintScriptLexer(), PrintScriptParser(), PrintScriptValidator(), PrintScriptLinter()).run(
            version,
            source(text),
            config(lintConfig, defaultLint),
            reporter(text, issues),
        )
        if (issues.any { it.error }) return EditorResult(text, issues, null)
        val formatted = format(text, version, formatConfig)
        issues.addAll(formatted.issues)
        formatted.formattedText?.let { output ->
            issues.addAll(FormattingDifferences.find(text, output))
        }
        return EditorResult(text, issues, formatted.formattedText)
    }

    fun format(text: String, version: String, formatConfig: Path?): EditorResult {
        val issues = mutableListOf<EditorIssue>()
        val output = StringBuilder()
        FormatterRunner(PrintScriptLexer(), PrintScriptParser(), PrintScriptFormatter()).run(
            version,
            source(text),
            object : OutputWriter {
                override fun write(input: Sequence<String>) {
                    input.forEach(output::append)
                }
            },
            config(formatConfig, defaultFormat),
            reporter(text, issues),
        )
        // The formatter omits the last newline. Preserve the editor's EOF convention.
        val formatted = if (issues.any { it.error }) {
            null
        } else {
            output.toString().let {
                if (text.endsWith("\n") && !it.endsWith("\n")) "$it\n" else it
            }
        }
        return EditorResult(text, issues, formatted)
    }

    private fun source(text: String): InputReader = object : InputReader {
        override fun read(): Sequence<Char> = text.asSequence().onEach { checkCanceled.run() }
    }

    private fun config(path: Path?, defaults: Collection<Rule>): ConfigReader =
        path?.let(::JsonConfigReader) ?: object : ConfigReader {
            override fun read(): Outcome<Collection<Rule>, Diagnostic> = Outcome.Ok(defaults)
        }

    private fun reporter(text: String, issues: MutableList<EditorIssue>): DiagnosticReporter =
        object : DiagnosticReporter {
            override fun report(diagnostic: Diagnostic) {
                val start = (diagnostic.span?.start?.index ?: 0).coerceIn(0, text.length)
                val end = (diagnostic.span?.end?.index ?: start).coerceIn(start, text.length)
                issues.add(
                    EditorIssue(
                        start,
                        end,
                        "${diagnostic.category.name}: ${diagnostic.message}",
                        diagnostic.severity.name == "Error",
                        false,
                    ),
                )
            }
        }

    private companion object {
        val defaultLint = listOf(Rule("identifier_format", StringRuleValue("camel case")))
        val defaultFormat = listOf(Rule("enforce-spacing-around-equals", BooleanRuleValue(true)))
    }
}

data class EditorIssue(val start: Int, val end: Int, val message: String, val error: Boolean, val formatting: Boolean)

data class EditorResult(val source: String, val issues: List<EditorIssue>, val formattedText: String?)
