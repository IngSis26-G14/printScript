package util

import common.model.diagnostic.Diagnostic
import common.model.node.Node
import common.type.outcome.Outcome
import java.nio.file.Files
import java.nio.file.Path

internal object GoldenTester {

    fun read(path: Path): String {
        return Files.readString(path)
            .replace("\r\n", "\n")
            .replace("\r", "\n")
    }

    fun format(results: Sequence<Outcome<Node, Diagnostic>>): String {
        val lines = mutableListOf<String>()

        for (outcome in results) {
            when (outcome) {
                is Outcome.Ok -> {
                    val node = outcome.value
                    lines.addAll(TestNode(node).format())
                }

                is Outcome.Error -> {
                    val diagnostic = outcome.error
                    lines.add(diagnostic.format())
                }
            }
        }

        return lines.joinToString("\n")
    }
}
