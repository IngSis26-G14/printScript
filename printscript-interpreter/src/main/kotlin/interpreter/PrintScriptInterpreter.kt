package interpreter

import Interpreter
import common.io.reader.env.EnvReader
import common.io.reader.input.InputReader
import common.io.writer.OutputWriter
import common.model.diagnostic.Diagnostic
import common.model.node.Node
import interpreter.internal.ExpressionEvaluator
import interpreter.internal.InMemorySymbolTable
import interpreter.internal.StatementExecutor
import interpreter.internal.diagnostic.ConfigurationDiagnostic

class PrintScriptInterpreter : Interpreter {
    override fun interpret(
        version: String,
        nodes: Sequence<Node>,
        input: InputReader,
        output: OutputWriter,
        env: EnvReader,
    ): Sequence<Diagnostic> = sequence {
        if (version != SUPPORTED_VERSION) {
            yield(ConfigurationDiagnostic("Unsupported PrintScript version: $version"))
            return@sequence
        }

        val symbols = InMemorySymbolTable()
        val evaluator = ExpressionEvaluator(symbols)
        val executor = StatementExecutor(symbols, evaluator, input, output, env)

        nodes.forEach { node ->
            executor.execute(node)?.let { yield(it) }
        }
    }.constrainOnce()

    private companion object {
        const val SUPPORTED_VERSION = "1.0"
    }
}
