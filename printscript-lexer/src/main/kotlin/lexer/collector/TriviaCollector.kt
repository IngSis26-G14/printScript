package lexer.collector

import common.model.span.Span
import common.model.trivia.NewlineTrivia
import common.model.trivia.SpaceTrivia
import common.model.trivia.TabTrivia
import common.model.trivia.Trivia
import lexer.SourceCursor

internal class TriviaCollector {

    fun collect(cursor: SourceCursor): List<Trivia> {
        val trivia = mutableListOf<Trivia>()

        while (cursor.peek()?.isWhitespace() == true) {
            val start = cursor.currentPosition()
            val character = cursor.advance()

            val type = when (character) {
                '\n' -> NewlineTrivia
                '\t' -> TabTrivia
                else -> SpaceTrivia
            }

            trivia += Trivia(
                type = type,
                lexeme = character.toString(),
                span = Span(start, cursor.currentPosition()),
            )
        }

        return trivia
    }
}
