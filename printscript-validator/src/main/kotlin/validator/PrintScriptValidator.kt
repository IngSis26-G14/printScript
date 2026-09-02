package validator

import Validator
import common.buffer.NodeBuffer
import common.model.diagnostic.Diagnostic
import common.model.node.Node
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitorContext
import common.type.option.Option
import common.type.outcome.Outcome
import validator.model.error.ConfigurationError
import validator.table.VisitorTableRegistry
import validator.util.DefaultStaticSymbolTable
import validator.util.StaticSymbolTable

class PrintScriptValidator : Validator {

    override fun validate(
        version: String,
        nodes: Sequence<Node>,
    ): Sequence<Outcome<Node, Diagnostic>> {
        return sequence {
            when (val table = getVisitorTable(version)) {
                is Option.Some -> {
                    for (node in validateNodes(nodes, table.value)) {
                        yield(node)
                    }
                }
                is Option.None -> {
                    yield(Outcome.Error(buildConfigurationError(version)))
                    return@sequence
                }
            }
        }
    }

    private fun getVisitorTable(version: String): Option<ContextVisitorTable> {
        return VisitorTableRegistry.get(version)
    }

    private fun buildConfigurationError(version: String): Diagnostic {
        return ConfigurationError("Unsupported language version '$version'")
    }

    private fun validateNodes(
        nodes: Sequence<Node>,
        table: ContextVisitorTable,
    ): Sequence<Outcome<Node, Diagnostic>> {
        return sequence {
            var context = VisitorContext()

            context = context.register(StaticSymbolTable::class, DefaultStaticSymbolTable())

            val buffer = NodeBuffer(nodes)

            while (buffer.hasNext()) {
                val node = buffer.next()
                val visit = table.dispatch(node, context)

                context = visit.context

                when (val outcome = visit.outcome) {
                    is Outcome.Ok -> {
                        yield(Outcome.Ok(node))
                    }
                    is Outcome.Error -> {
                        yield(outcome)
                    }
                }
            }
        }
    }
}
