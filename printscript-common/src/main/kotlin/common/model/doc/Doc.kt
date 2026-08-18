package common.model.doc

import common.model.span.Span
import common.model.trivia.Trivia

data class Doc(
    val text: String,
    val span: Span,
    val leading: Collection<Trivia>,
    val trailing: Collection<Trivia>,
) {
    fun format(): String {
        val leadingText = leading.joinToString("") { it.lexeme }
        val trailingText = trailing.joinToString("") { it.lexeme }
        return leadingText + text + trailingText
    }
}
