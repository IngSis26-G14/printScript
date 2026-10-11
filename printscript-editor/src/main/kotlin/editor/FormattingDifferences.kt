package editor

/** Compare whitespace runs between identical non-whitespace characters, including string contents. */
internal object FormattingDifferences {
    fun find(source: String, formatted: String): List<EditorIssue> {
        if (source == formatted) return emptyList()
        val issues = mutableListOf<EditorIssue>()
        var originalIndex = 0
        var formattedIndex = 0
        while (originalIndex < source.length || formattedIndex < formatted.length) {
            val start = originalIndex
            val expectedStart = formattedIndex
            while (originalIndex < source.length && source[originalIndex].isWhitespace()) originalIndex++
            while (formattedIndex < formatted.length && formatted[formattedIndex].isWhitespace()) formattedIndex++
            if (source.substring(start, originalIndex) != formatted.substring(expectedStart, formattedIndex)) {
                issues.add(EditorIssue(start, originalIndex, "Formatting differs from the configured rules", false, true))
            }
            val original = source.getOrNull(originalIndex)
            val expected = formatted.getOrNull(formattedIndex)
            if (original != expected) {
                // Avoid incorrect ranges if a future formatter also rewrites non-whitespace text.
                return listOf(EditorIssue(0, source.length, "File differs from the configured formatting", false, true))
            }
            if (original == null) break
            originalIndex++
            formattedIndex++
        }
        return issues
    }
}
