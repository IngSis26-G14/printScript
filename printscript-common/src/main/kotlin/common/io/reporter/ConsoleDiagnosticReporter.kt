package common.io.reporter

import common.model.diagnostic.Diagnostic

class ConsoleDiagnosticReporter : DiagnosticReporter {

    override fun report(diagnostic: Diagnostic) {
        println(diagnostic.format())
    }
}
