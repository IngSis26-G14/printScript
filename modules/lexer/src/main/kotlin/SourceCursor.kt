import common.source.SourcePosition
import java.io.Reader
import java.util.ArrayDeque

internal class SourceCursor(
    source: Reader,
    ) {

    private val reader = source.buffered()
    private var lookahead = ArrayDeque<Char>()
    private var reachedEnd = false

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

    fun currentPosition(): SourcePosition =
        SourcePosition(
            line = line,
            column = column,
            offset = offset
        )

    private fun fillLookahead(requiredSize: Int) {
        while (lookahead.size < requiredSize && !reachedEnd) {
            val next = reader.read()

            if (next == -1) {
                reachedEnd = true
            } else {
                lookahead.add(next.toChar())
            }
        }
    }
}
