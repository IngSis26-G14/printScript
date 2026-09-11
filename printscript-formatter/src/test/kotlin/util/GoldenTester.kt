package util

import common.model.diagnostic.Diagnostic
import common.model.doc.Doc
import common.type.outcome.Outcome
import java.nio.file.Files
import java.nio.file.Path

internal object GoldenTester {

    fun read(path: Path): String {
        return Files.readString(path)
            .replace("\r\n", "\n")
            .replace("\r", "\n")
    }

    fun format(results: Sequence<Outcome<Doc, Diagnostic>>): String {
        val lines = mutableListOf<String>()

        for (outcome in results) {
            when (outcome) {
                is Outcome.Ok -> lines.add(outcome.value.format())
                is Outcome.Error -> lines.add(outcome.error.format())
            }
        }

        return lines.joinToString("")
    }
}
