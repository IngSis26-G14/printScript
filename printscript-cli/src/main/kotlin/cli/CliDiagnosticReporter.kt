package cli

import common.io.reporter.DiagnosticReporter
import common.model.diagnostic.Diagnostic
import java.io.PrintStream

class CliDiagnosticReporter(
    private val output: PrintStream,
) : DiagnosticReporter {
    var hasDiagnostics: Boolean = false
        private set

    override fun report(diagnostic: Diagnostic) {
        hasDiagnostics = true
        output.println(diagnostic.format())
    }
}
