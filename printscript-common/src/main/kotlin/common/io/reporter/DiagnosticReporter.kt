package common.io.reporter

import common.model.diagnostic.Diagnostic

interface DiagnosticReporter {
    fun report(diagnostic: Diagnostic)
}
