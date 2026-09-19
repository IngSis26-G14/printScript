package interpreter

import Interpreter
import common.io.reader.env.EnvReader
import common.io.reader.input.InputReader
import common.io.writer.OutputWriter
import common.model.diagnostic.Diagnostic
import common.model.node.Node
import interpreter.internal.ExpressionEvaluator
import interpreter.internal.InMemorySymbolTable
import interpreter.internal.RuntimeReader
import interpreter.internal.StatementExecutor
import interpreter.internal.VersionSupport
import interpreter.internal.diagnostic.ConfigurationDiagnostic
import interpreter.internal.diagnostic.InterpreterDiagnostic
import interpreter.internal.diagnostic.Runtime

class DefaultPrintScriptInterpreter : Interpreter {
    override fun interpret(
        version: String,
        nodes: Sequence<Node>,
        input: InputReader,
        output: OutputWriter,
        env: EnvReader,
    ): Sequence<Diagnostic> = sequence {
        if (version !in setOf("1.0", "1.1")) {
            yield(ConfigurationDiagnostic("Unsupported PrintScript version: $version"))
            return@sequence
        }

        val symbols = InMemorySymbolTable()
        val evaluator = ExpressionEvaluator(symbols, RuntimeReader(input, output, env))
        val executor = StatementExecutor(symbols, evaluator, output)

        nodes.forEach { node ->
            val unsupported = VersionSupport.unsupportedNode(node, version)
            if (unsupported != null) {
                yield(InterpreterDiagnostic("Feature requires PrintScript 1.1", span = unsupported.span))
                return@sequence
            }
            executor.execute(node)?.let {
                yield(it)
                if (it.category == Runtime) return@sequence
            }
        }
    }.constrainOnce()
}
