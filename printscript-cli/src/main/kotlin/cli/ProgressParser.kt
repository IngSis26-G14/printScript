package cli

import Parser
import common.model.diagnostic.Diagnostic
import common.model.node.Node
import common.model.token.Token
import common.type.outcome.Outcome
import java.io.PrintStream

class ProgressParser(
    private val delegate: Parser,
    private val totalCharacters: Long,
    private val output: PrintStream,
) : Parser {
    override fun parse(
        version: String,
        tokens: Sequence<Token>,
    ): Sequence<Outcome<Node, Diagnostic>> = sequence {
        var lastPercentage = -1

        fun showProgress(consumedCharacters: Long) {
            val percentage = if (totalCharacters == 0L) {
                100
            } else {
                (consumedCharacters * 100L / totalCharacters)
                    .coerceIn(0L, 100L)
                    .toInt()
            }

            if (percentage != lastPercentage) {
                lastPercentage = percentage
                output.print("\rParsing: $percentage%")
            }
        }

        showProgress(0)
        val trackedTokens = tokens.onEach { token ->
            showProgress(token.span.end.index.toLong())
        }

        var errorReported = false
        for (outcome in delegate.parse(version, trackedTokens)) {
            if (outcome is Outcome.Error) {
                errorReported = true
                output.println()
            }
            yield(outcome)
        }

        if (!errorReported) {
            showProgress(totalCharacters)
            output.println()
        }
    }.constrainOnce()
}
