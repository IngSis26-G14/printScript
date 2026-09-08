package formatter.model.context

import common.model.trivia.Trivia

internal data class PrintlnState(
    val pendingNewlines: List<Trivia> = emptyList(),
    val hadPreviousPrintln: Boolean = false,
)
