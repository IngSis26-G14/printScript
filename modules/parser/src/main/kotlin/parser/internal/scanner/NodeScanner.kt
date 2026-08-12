package parser.internal.scanner

import common.type.option.Option
import common.type.option.getOrElse
import common.type.option.maxByOrEqual
import common.type.outcome.Outcome
import common.type.option.Option.None
import parser.internal.buffer.TokenBuffer
import parser.internal.model.scan.NodeScan
import parser.internal.table.GrammarTable


internal class NodeScanner {

    fun scan(
        buffer: TokenBuffer,
        table: GrammarTable,
    ): NodeScan {
        if (!buffer.hasNext()) return NodeScan.Empty

        var bestNode: Option<NodeScan.Ok> = None
        var bestError: Option<NodeScan.Error> = None
        var bestConsumed = 0
        var sliceSize = 1

        while (buffer.hasNext(sliceSize)) {
            val slice = buffer.peek(sliceSize).toList()

            when (val outcome = table.dispatchStatement(slice)) {
                is Outcome.Ok -> {
                    val match = outcome.value
                    val candidate = NodeScan.Ok(match.node, match.consumed)

                    if (match.consumed > bestConsumed) {
                        bestNode = Option.Some(candidate)
                        bestConsumed = match.consumed
                        sliceSize++
                    } else {
                        break
                    }
                }
                is Outcome.Error -> {
                    val fail = outcome.error
                    val candidate = NodeScan.Error(fail.message, fail.category, fail.consumed)
                    bestError = bestError.maxByOrEqual({ it.consumed }, candidate)
                    sliceSize++
                }
            }
        }

        return when {
            bestNode is Option.Some && bestError is Option.Some -> {
                if (bestError.value.consumed > bestNode.value.consumed) {
                    bestError.value
                } else {
                    bestNode.value
                }
            }
            else -> bestNode.getOrElse { bestError.getOrElse { NodeScan.Empty } }
        }
    }
}
