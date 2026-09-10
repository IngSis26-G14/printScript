package util

import common.model.token.Token

internal object TestSnippetRegistry {
    private val registry = mutableMapOf<String, List<Token>>()

    fun register(testName: String, tokens: List<Token>) {
        registry[testName] = tokens
    }

    fun get(testName: String): Collection<Token> =
        registry[testName] ?: error("No tokens registered for test: $testName")

    fun clear() {
        registry.clear()
    }
}
