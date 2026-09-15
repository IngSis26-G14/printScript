package util

import common.model.node.Node

internal object TestSnippetRegistry {
    private val registry = mutableMapOf<String, List<Node>>()

    fun register(testName: String, nodes: List<Node>) {
        registry[testName] = nodes
    }

    fun get(testName: String): Collection<Node> =
        registry[testName] ?: error("No nodes registered for test: $testName")

    fun clear() {
        registry.clear()
    }
}
