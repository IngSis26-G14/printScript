package util

import common.model.node.Node
import common.model.trivia.Trivia

data class TestNode(val node: Node) {

    fun format(indent: String = ""): List<String> {
        val lines = mutableListOf<String>()

        when (node) {
            is Node.Leaf -> {
                val value = escape(node.value.format())
                lines.add("$indent${node.type} ${node.span.format()} value='$value'")

                if (node.leading.isNotEmpty()) {
                    lines.add("$indent  leading:")
                    node.leading.forEach { lines.add("$indent    ${formatTrivia(it)}") }
                }

                if (node.trailing.isNotEmpty()) {
                    lines.add("$indent  trailing:")
                    node.trailing.forEach { lines.add("$indent    ${formatTrivia(it)}") }
                }
            }

            is Node.Composite -> {
                lines.add("$indent${node.type} ${node.span.format()}")

                if (node.children.isNotEmpty()) {
                    lines.add("$indent  children:")
                    node.children.forEach { child ->
                        lines.addAll(TestNode(child).format("$indent    "))
                    }
                }
            }
        }

        return lines
    }

    private fun formatTrivia(trivia: Trivia): String {
        return "${trivia.span.format()} ${trivia.type} '${escape(trivia.lexeme)}'"
    }

    private fun escape(text: String): String {
        return text
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("'", "\\'")
    }
}
