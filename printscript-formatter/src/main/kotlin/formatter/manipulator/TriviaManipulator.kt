package formatter.manipulator

import common.model.node.Node
import common.model.trivia.Trivia
import common.model.trivia.TriviaType

internal object TriviaManipulator {

    fun extractTrailing(node: Node, triviaType: TriviaType): Pair<Node, List<Trivia>> {
        return when (node) {
            is Node.Leaf -> {
                val extracted = node.trailing.filter { it.type == triviaType }
                val cleaned = node.trailing.filter { it.type != triviaType }
                Pair(node.copy(trailing = cleaned), extracted)
            }
            is Node.Composite -> {
                val children = node.children.toList()
                if (children.isEmpty()) return Pair(node, emptyList())

                val (lastUpdated, extracted) = extractTrailing(children.last(), triviaType)
                val updated = children.dropLast(1) + lastUpdated
                Pair(node.copy(children = updated), extracted)
            }
        }
    }

    fun extractLeading(node: Node, triviaType: TriviaType): Pair<Node, List<Trivia>> {
        return when (node) {
            is Node.Leaf -> {
                val extracted = node.leading.filter { it.type == triviaType }
                val cleaned = node.leading.filter { it.type != triviaType }
                Pair(node.copy(leading = cleaned), extracted)
            }
            is Node.Composite -> {
                val children = node.children.toList()
                if (children.isEmpty()) return Pair(node, emptyList())

                val (firstUpdated, extracted) = extractLeading(children.first(), triviaType)
                val updated = listOf(firstUpdated) + children.drop(1)
                Pair(node.copy(children = updated), extracted)
            }
        }
    }

    fun addLeading(node: Node, trivia: List<Trivia>): Node {
        if (trivia.isEmpty()) return node

        return when (node) {
            is Node.Leaf -> {
                node.copy(leading = trivia + node.leading)
            }
            is Node.Composite -> {
                val children = node.children.toList()
                if (children.isEmpty()) return node

                val firstUpdated = addLeading(children.first(), trivia)
                val updated = listOf(firstUpdated) + children.drop(1)
                node.copy(children = updated)
            }
        }
    }

    fun removeLeading(node: Node, triviaType: TriviaType): Node {
        return when (node) {
            is Node.Leaf -> {
                node.copy(leading = node.leading.filter { it.type != triviaType })
            }
            is Node.Composite -> {
                val children = node.children.toList()
                if (children.isEmpty()) return node

                val firstUpdated = removeLeading(children.first(), triviaType)
                val updated = listOf(firstUpdated) + children.drop(1)
                node.copy(children = updated)
            }
        }
    }

    fun removeTrailing(node: Node, triviaType: TriviaType): Node {
        return when (node) {
            is Node.Leaf -> {
                node.copy(trailing = node.trailing.filter { it.type != triviaType })
            }
            is Node.Composite -> {
                val children = node.children.toList()
                if (children.isEmpty()) return node

                val lastUpdated = removeTrailing(children.last(), triviaType)
                val updated = children.dropLast(1) + lastUpdated
                node.copy(children = updated)
            }
        }
    }

    fun addTrailing(node: Node, trivia: List<Trivia>): Node {
        if (trivia.isEmpty()) return node

        return when (node) {
            is Node.Leaf -> {
                node.copy(trailing = node.trailing + trivia)
            }
            is Node.Composite -> {
                val children = node.children.toList()
                if (children.isEmpty()) return node

                val lastUpdated = addTrailing(children.last(), trivia)
                val updated = children.dropLast(1) + lastUpdated
                node.copy(children = updated)
            }
        }
    }
}
