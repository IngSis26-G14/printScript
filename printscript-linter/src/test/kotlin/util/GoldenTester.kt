package util

import common.model.diagnostic.Diagnostic
import java.nio.file.Files
import java.nio.file.Path

internal object GoldenTester {

    fun read(path: Path): String {
        return Files.readString(path)
            .replace("\r\n", "\n")
            .replace("\r", "\n")
    }

    fun format(results: Sequence<Diagnostic>): String {
        val lines = mutableListOf<String>()

        for (diagnostic in results) {
            lines.add(diagnostic.format())
        }

        return lines.joinToString("\n")
    }
}
