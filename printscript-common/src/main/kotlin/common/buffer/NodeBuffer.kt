package common.buffer

import common.model.node.Node

class NodeBuffer(nodes: Sequence<Node>) {
    private val iterator = nodes.iterator()
    private val inner = ArrayDeque<Node>()

    init {
        fill()
    }

    private fun fill() {
        while (inner.size < LOOKAHEAD && iterator.hasNext()) {
            inner.addLast(iterator.next())
        }
    }

    fun peek(n: Int = 1): Collection<Node> {
        fill()
        return inner.take(n)
    }

    fun next(): Node {
        if (inner.isEmpty()) fill()
        return inner.removeFirst().also { fill() }
    }

    fun hasNext(): Boolean = inner.isNotEmpty() || iterator.hasNext()

    companion object {
        private const val LOOKAHEAD = 64
    }
}
