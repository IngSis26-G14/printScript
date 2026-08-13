package lexer

import common.model.span.Position
import java.util.ArrayDeque

internal class SourceCursor(source: Sequence<Char>) {
    private val iterator = source.iterator()
    private val lookahead = ArrayDeque<Char>()

    var offset: Int = 0
        private set

    var line: Int = 1
        private set

    var column: Int = 1
        private set

    fun isAtEnd(): Boolean =
        peek() == null

    fun peek(distance: Int = 0): Char? {
        require(distance >= 0) {
            "Peek distance cannot be negative"
        }

        fillLookahead(distance + 1)
        return lookahead.elementAtOrNull(distance)
    }

    fun advance(): Char {
        val character = checkNotNull(peek()) {
            "Cannot advance beyond the end of the source"
        }

        lookahead.removeFirst()
        offset++

        if (character == '\n') {
            line++
            column = 1
        } else {
            column++
        }
        return character
    }

    fun currentPosition(): Position =
        Position(
            line = line,
            column = column,
            index = offset,
        )

    private fun fillLookahead(requiredSize: Int) {
        while (lookahead.size < requiredSize && iterator.hasNext()) {
            lookahead.add(iterator.next())
        }
    }
}