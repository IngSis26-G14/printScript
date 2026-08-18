package common.model.trivia

import common.model.span.Span

data class Trivia(
    val type: TriviaType,
    val lexeme: String,
    val span: Span,
)
