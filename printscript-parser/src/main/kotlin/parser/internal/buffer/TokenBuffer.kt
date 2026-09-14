package parser.internal.buffer

import common.model.token.Token

/** A lazy view over a token stream. Indices are relative to this cursor. */
internal interface TokenCursor {
    fun tokenAt(index: Int = 0): Token?

    fun drop(count: Int): TokenCursor

    fun isEmpty(): Boolean = tokenAt() == null
}

internal inline fun TokenCursor.getOrElse(index: Int, defaultValue: () -> Token): Token =
    tokenAt(index) ?: defaultValue()

/** Retains only tokens at or after the current top-level statement. */
internal class TokenBuffer(tokens: Sequence<Token>) : TokenCursor {
    private val iterator = tokens.iterator()
    private val buffered = ArrayList<Token>()

    override fun tokenAt(index: Int): Token? {
        require(index >= 0) { "Token index cannot be negative" }
        while (buffered.size <= index && iterator.hasNext()) {
            buffered.add(iterator.next())
        }
        return buffered.getOrNull(index)
    }

    override fun drop(count: Int): TokenCursor {
        require(count >= 0) { "Token offset cannot be negative" }
        return TokenCursorView(this, count)
    }

    fun advance(count: Int) {
        require(count in 0..buffered.size) { "Cannot advance past buffered tokens" }
        buffered.subList(0, count).clear()
    }
}

private class TokenCursorView(
    private val source: TokenBuffer,
    private val offset: Int,
) : TokenCursor {
    override fun tokenAt(index: Int): Token? {
        require(index >= 0) { "Token index cannot be negative" }
        return source.tokenAt(offset + index)
    }

    override fun drop(count: Int): TokenCursor {
        require(count >= 0) { "Token offset cannot be negative" }
        return TokenCursorView(source, offset + count)
    }
}
