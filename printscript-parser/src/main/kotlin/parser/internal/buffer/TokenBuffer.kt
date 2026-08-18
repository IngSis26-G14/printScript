package parser.internal.buffer

import common.model.token.Token

internal class TokenBuffer(tokens: Sequence<Token>) {
    private val iterator = tokens.iterator()
    private val inner = ArrayDeque<Token>()

    init {
        fill()
    }

    private fun fill() {
        while (inner.size < LOOKAHEAD && iterator.hasNext()) {
            inner.addLast(iterator.next())
        }
    }

    fun peek(n: Int = 1): Collection<Token> {
        fill()
        return inner.take(n)
    }

    fun advance(n: Int = 1) {
        repeat(n.coerceAtMost(inner.size)) { inner.removeFirst() }
        fill()
    }

    fun hasNext(n: Int = 1): Boolean = peek(n).size == n

    companion object {
        private const val LOOKAHEAD = 64
    }
}
