package formatter.model.context

import common.model.trivia.Trivia

internal data class LineBreakState(
    val pendingNewlines: List<Trivia> = emptyList(),
    val isFirstNode: Boolean = true,
)
