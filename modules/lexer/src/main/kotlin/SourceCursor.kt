import common.source.SourcePosition

internal class SourceCursor (
    private val source: String,
    ) {

    var offset: Int = 0
        private set

    var line: Int = 1
        private set

    var column: Int = 1
        private set

    fun isAtEnd(): Boolean {
        return offset >= source.length
    }

    fun peek(): Char? {
        return source.getOrNull(offset)
    }

    fun advance(): Char {
        check(!isAtEnd()){
            "Cannot advance beyond the end of the source."
        }

        val character = source[offset]
        offset++

        if(character == '\n'){
            line++
            column = 1
        } else {
            column++
        }
        return character
    }

    fun currentPosition(): SourcePosition{
        return SourcePosition(
            line = line,
            column = column,
            offset = offset
        )
    }

}