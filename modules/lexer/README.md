# PrintScript Lexer

The lexer module performs lexical analysis for PrintScript 1.0. It reads source code as a stream of characters and turns it into a stream of `Token` objects that can be consumed by the parser and, indirectly, by the interpreter, formatter, and static analyzer.

The lexer answers questions such as:

- Is `let` a keyword or an identifier?
- Is `12.5` a number literal?
- Where in the source did a token begin and end?
- Is `"Hello"` a valid string literal?
- Does an input character belong to any valid PrintScript token?

It does not decide whether a sequence of valid tokens forms a valid program. Syntax, type rules, variable declarations, operator precedence, and evaluation belong to later stages of the language pipeline.

## Language pipeline

```text
Reader
  |
  v
Lexer  -- Sequence<Token> -->  Parser  -->  AST  -->  Interpreter
                              |   |
                              |   +-----> Formatter
                              +---------> Static analyzer
```

The lexer is the only stage concerned with grouping raw characters into lexical units. For example:

```printscript
println("Hello" + 12.5);
```

is emitted approximately as:

| Token type | Lexeme |
| --- | --- |
| `PRINTLN` | `println` |
| `LEFT_PARENTHESIS` | `(` |
| `STRING_LITERAL` | `"Hello"` |
| `ADDITION_OPERATOR` | `+` |
| `NUMBER_LITERAL` | `12.5` |
| `RIGHT_PARENTHESIS` | `)` |
| `SEMICOLON` | `;` |
| `EOF` | an empty string |

Each row represents one `Token`. A literal is not emitted as separate "value" and "type" tokens. Instead, the same token contains its classification, original lexeme, and source range.

## Module boundary

The module is declared in Gradle as `:modules:lexer` and depends on `:modules:common`:

```text
lexer --> common
```

The common module supplies the shared data structures used at module boundaries:

- `common.token.Token`
- `common.token.TokenType`
- `common.source.SourcePosition`
- `common.source.SourceRange`

The lexer module exposes `Lexer`, `PrintScriptLexer`, and `LexicalException`. Its cursor and individual lexical rules are `internal` implementation details.

The parser should depend on the `Lexer` interface when possible rather than on implementation details such as `SourceCursor` or a particular rule class.

## Build and tooling

The repository configures the project with:

- Kotlin/JVM 2.2.0.
- Java 21 through Gradle's JVM toolchain.
- Maven Central for dependency resolution.
- `kotlin-test` for automated tests running on the JUnit Platform.
- Kover for code-coverage reporting and verification.

The lexer has no external runtime library dependency beyond the project's common module. The root Kover configuration aggregates the project modules and currently declares an 80 percent minimum coverage bound.

## Source layout

```text
modules/lexer/
├── build.gradle
├── README.md
└── src/
    ├── main/kotlin/lexer/
    │   ├── Lexer.kt
    │   ├── PrintScriptLexer.kt
    │   ├── LexicalException.kt
    │   ├── SourceCursor.kt
    │   └── rules/
    │       ├── LexerRule.kt
    │       ├── NumberRule.kt
    │       ├── StringRule.kt
    │       ├── SymbolRule.kt
    │       └── WordRule.kt
    └── test/kotlin/
        └── PrintScriptLexerTest.kt
```

## Public API

### `Lexer`

`Lexer` is the abstraction exposed to consumers:

```kotlin
interface Lexer {
    fun tokenize(source: Reader): Sequence<Token>
}
```

It accepts a `Reader` so callers can supply source code from a file, string, network stream, or another character source without changing the lexer.

### `PrintScriptLexer`

`PrintScriptLexer` is the PrintScript 1.0 implementation. Its public no-argument constructor installs the standard rules:

1. `StringRule`
2. `NumberRule`
3. `WordRule`
4. `SymbolRule`

Its primary constructor is internal and accepts a rule list. This permits module tests and future version-specific construction without exposing rule implementations as public API.

### Basic usage

```kotlin
val lexer: Lexer = PrintScriptLexer()

val tokens = lexer.tokenize(
    "let result: number = 12.5;".reader(),
)

tokens.forEach { token ->
    println("${token.type}: ${token.lexeme}")
}
```

For a file, consume the sequence while the reader is open:

```kotlin
file.bufferedReader().use { reader ->
    val tokens = lexer.tokenize(reader)
    parser.parse(tokens)
}
```

The lexer does not close the supplied `Reader`; resource ownership remains with the caller.

## Streaming and memory behavior

Tokenization is lazy. `tokenize` returns a sequence created with Kotlin's `sequence` builder, and the lexer does not scan the complete source until a consumer requests all tokens.

The implementation constrains the returned sequence to one iteration because a `Reader` is a forward-only source:

```kotlin
sequence {
    // Read and yield tokens incrementally.
}.constrainOnce()
```

Consequences of this design:

- Creating the sequence does not necessarily read or validate the source.
- Lexical exceptions can be raised while the sequence is being consumed.
- Iterating the same token sequence twice is not supported.
- The reader must remain open throughout consumption.
- Production consumers should process tokens incrementally instead of calling `toList()` on a very large source.
- Using `toList()` in unit tests is appropriate because test inputs are intentionally small.

`SourceCursor` wraps the reader in a buffered reader and maintains only the lookahead required by lexical rules. The complete source is never copied into one string. Memory use is therefore bounded by reader buffering, lookahead, and the size of the current token rather than by the size of the complete program.

## Token model

A token contains three fields:

```kotlin
data class Token(
    val type: TokenType,
    val lexeme: String,
    val range: SourceRange,
)
```

### Type

`type` describes the lexical category. The parser normally makes decisions based on this field.

### Lexeme

`lexeme` is the exact text consumed from the source. Examples include:

```text
Token(LET, "let", ...)
Token(IDENTIFIER, "result", ...)
Token(NUMBER_LITERAL, "12.5", ...)
Token(STRING_LITERAL, "'Hello'", ...)
Token(ADDITION_OPERATOR, "+", ...)
```

String lexemes retain their opening and closing quotation marks. The parser can later turn `'Hello'` or `"Hello"` into an AST string value containing `Hello`. Similarly, the parser can convert a number lexeme such as `12.5` into its chosen numeric representation.

Keeping the raw lexeme is useful for diagnostics, formatting, source reconstruction, and deferring value conversion to the correct compiler stage.

### Source range

`range` identifies where the lexeme appeared. Positions contain:

- `line`: one-based line number.
- `column`: one-based column number.
- `offset`: zero-based character offset from the start of the source.

Ranges are end-exclusive. For the token `let` at the beginning of a file:

```text
start = SourcePosition(line = 1, column = 1, offset = 0)
end   = SourcePosition(line = 1, column = 4, offset = 3)
```

The end position points immediately after the token. This convention makes ranges composable and makes token length equal to `end.offset - start.offset`.

Only `\n` advances the cursor to a new line. Other consumed characters advance the column. The lexer skips all characters for which Kotlin's `Char.isWhitespace()` returns `true`.

## Supported token types

### Keywords

| Source | Token type |
| --- | --- |
| `let` | `LET` |
| `println` | `PRINTLN` |

### Type names

| Source | Token type |
| --- | --- |
| `number` | `TYPE_NUMBER` |
| `string` | `TYPE_STRING` |

### Identifiers

An identifier starts with a letter or underscore. Remaining characters may be letters, digits, or underscores.

Examples:

```text
name
lastName
result2
_temporary
```

The complete word is consumed before keyword classification. Therefore `let` is a keyword, while `letter` and `let2` are identifiers.

The current implementation uses Kotlin's `isLetter` and `isLetterOrDigit`, so it accepts Unicode letters and digits. If the language is later restricted to ASCII identifiers, the rule should use explicit `a..z`, `A..Z`, and `0..9` checks.

### Number literals

The current number grammar is conceptually:

```text
digit+ ('.' digit+)?
```

Examples accepted as one `NUMBER_LITERAL` token:

```text
0
12
12.5
0.25
```

The decimal point is consumed only when at least one digit follows it. As a result:

- `12.` becomes `NUMBER_LITERAL("12")` followed by an unexpected `.` error.
- `.5` begins with an unexpected `.` error.
- Exponential notation such as `1e10` is not implemented as a single number literal.
- A leading sign is not part of the literal.

For example, `-5` is emitted as:

```text
SUBTRACTION_OPERATOR("-")
NUMBER_LITERAL("5")
```

The parser determines whether `-` represents subtraction or unary negation. The lexer must not make that syntactic decision.

The implementation currently uses `Char.isDigit`. If PrintScript numbers must be ASCII-only, this should be narrowed to `character in '0'..'9'` consistently in matching and reading.

### String literals

Strings may use matching single or double quotes:

```printscript
'Hello'
"Hello"
```

Both forms produce `STRING_LITERAL`. Their raw lexemes retain the delimiters.

A string ends at the next occurrence of its opening delimiter. Reaching EOF or a newline first throws `LexicalException` for an unterminated string. Multiline strings are therefore not supported.

Escape-sequence handling is not currently implemented. Syntax such as `"a \"quoted\" value"` requires an explicit language decision and corresponding changes in both the lexer and parser before it can be supported reliably.

### Operators and delimiters

| Source | Token type |
| --- | --- |
| `=` | `ASSIGNMENT_OPERATOR` |
| `+` | `ADDITION_OPERATOR` |
| `-` | `SUBTRACTION_OPERATOR` |
| `*` | `MULTIPLICATION_OPERATOR` |
| `/` | `DIVISION_OPERATOR` |
| `:` | `COLON` |
| `;` | `SEMICOLON` |
| `(` | `LEFT_PARENTHESIS` |
| `)` | `RIGHT_PARENTHESIS` |

All currently supported symbols consist of one character. Multi-character operators would require longest-match behavior so that, for example, `==` is considered before `=`.

### End of file

Every successfully consumed source ends with exactly one `EOF` token. Its lexeme is empty, and its start and end positions both equal the position immediately after the final source character, including ignored trailing whitespace.

An empty source produces only `EOF` at line 1, column 1, offset 0.

## Rule architecture

Every lexical category implements:

```kotlin
internal interface LexerRule {
    fun matches(cursor: SourceCursor): Boolean
    fun read(cursor: SourceCursor): Token
}
```

The rule contract is:

1. `matches` may inspect the cursor but must not consume input.
2. `read` is called only when `matches` is true.
3. `read` consumes exactly one complete token.
4. `read` returns a token whose lexeme and range correspond to the consumed characters.
5. Invalid user input produces a lexical error; a violated rule precondition indicates an implementation error.

For every non-whitespace position, `PrintScriptLexer` chooses the first matching rule. After reading, it verifies that the cursor offset advanced. This protects the lazy loop from a faulty rule that returns a token without consuming input.

Whitespace and EOF are intentionally not `LexerRule` implementations:

- Whitespace is ignored and does not produce a token.
- EOF is emitted once by the coordinating lexer after all source characters have been consumed.

### Rule priority

The current rules have non-overlapping starting characters, but rule order becomes important when the language grows. For example, a future `//` comment rule and the current division rule both begin with `/`. The comment rule would need to appear before `SymbolRule`, or symbol matching would consume `/` first.

Priority should always be explicit and covered by tests when two rules can match the same prefix.

## Cursor behavior

`SourceCursor` centralizes all interaction with the reader. Rules must use `peek` and `advance` rather than reading directly.

Its operations are:

- `peek()`: inspect the current character without consuming it.
- `peek(distance)`: inspect a later character, used for bounded lookahead.
- `advance()`: consume one character and update offset, line, and column.
- `isAtEnd()`: report whether no character remains.
- `currentPosition()`: capture an immutable source position.

Centralizing movement guarantees that all rules update positions consistently. It also allows the storage strategy to remain independent of rule logic.

## Tokenization algorithm

For each requested token, `PrintScriptLexer` performs the following process:

```text
skip whitespace
      |
      v
at EOF? ---- yes ----> emit EOF and finish
      |
      no
      |
      v
find first matching rule
      |
      +---- none ----> throw LexicalException
      |
      v
read one token
      |
      v
verify cursor advanced
      |
      v
yield token and repeat
```

With a fixed, small rule set, scanning is linear in the number of source characters. The rule-selection cost is proportional to the number of registered rules, which is currently four.

## Lexical errors

`LexicalException` represents invalid source characters or incomplete lexical constructs. It extends `RuntimeException` and carries a `SourcePosition`.

### Unexpected characters

If no rule matches the current non-whitespace character, the lexer throws an error such as:

```text
Unexpected character '@' at line 1, column 5
```

### Unterminated strings

If a string reaches a newline or EOF before its closing delimiter, `StringRule` throws an error such as:

```text
Unterminated string at line 1, column 1
```

The stored position is currently the position of the opening quote.

Because tokenization is lazy, tests and consumers must request enough tokens to reach the invalid input:

```kotlin
val exception = assertFailsWith<LexicalException> {
    lexer.tokenize("let @".reader()).toList()
}
```

Calling only `lexer.tokenize(...)` constructs a sequence and does not guarantee that validation has occurred.

The assignment requires diagnostics to identify both the beginning and end of a problem. A future error model may therefore replace the single `SourcePosition` with `SourceRange`, or add a range while retaining compatibility.

## Interaction with the parser

The lexer reports lexical facts; the parser assigns grammatical meaning.

For example, the lexer emits:

```text
Token(NUMBER_LITERAL, "12.5", range)
Token(STRING_LITERAL, "'Hello'", range)
```

The parser can convert those tokens into AST values:

```text
NumberLiteral(value = 12.5)
StringLiteral(value = "Hello")
```

Value conversion should not destroy the original token information if later tools need precise diagnostics or formatting. Decimal representation is also a parser/interpreter design decision; `BigDecimal` is often preferable when exact decimal arithmetic is required.

The parser, not the lexer, is responsible for detecting conditions such as:

- Missing semicolons.
- Invalid declaration structure.
- Unbalanced parentheses.
- Invalid expression order.
- Operator precedence.
- Assignment to undeclared variables.
- Type incompatibility.
- Incorrect `println` arguments.

For example, `let value: number = "text";` is lexically valid even though a later semantic stage should reject the type mismatch.

## Interaction with other tools

### Interpreter

The interpreter should operate on the AST, not directly on raw tokens. It evaluates already parsed literal values, expressions, declarations, assignments, and `println` calls.

### Formatter

The current lexer discards whitespace rather than emitting trivia tokens. A formatter can reconstruct configured whitespace from the token/AST structure and source ranges. If exact lossless source round-tripping is required in the future, comments and whitespace may need to be preserved in a separate trivia model.

### Static analyzer

The analyzer works primarily from the AST and symbol/type information. Token and AST source ranges allow findings to refer back to precise locations in the original file.

### CLI and progress reporting

The CLI should keep the source reader open while parsing and can update progress as tokens or AST nodes are consumed. The lexer exposes offsets suitable for downstream progress calculations, but presenting progress is not a lexer responsibility.

## Extending the lexer

### Adding a keyword

1. Add the new entry to `TokenType` in the common module.
2. Add its spelling to `WordRule.reservedWords`.
3. Add tests proving the exact word is a keyword and longer words remain identifiers.

For example, adding `const` must not cause `constant` to become `CONST` plus `IDENTIFIER`.

### Adding a one-character symbol

1. Add its `TokenType`.
2. Add the character-to-type mapping in `SymbolRule`.
3. Add lexer tests for its type, lexeme, and range.

### Adding a new lexical category

1. Define any required token types in `common`.
2. Implement a stateless `LexerRule`.
3. Add the rule to the default list in `PrintScriptLexer` at the correct priority.
4. Test `matches`, consumption boundaries, lexeme preservation, ranges, and invalid input.
5. Add an integration test involving surrounding token categories.

### Adding comments

First decide whether comments should be discarded or preserved as trivia. A `//` rule must take priority over `/` as division. Block comments also need a defined unterminated-comment error and correct multiline position tracking.

### Adding multi-character operators

Use maximal munch: inspect all operators sharing the prefix and consume the longest valid one. For a language supporting both `=` and `==`, the input `==` must become one equality token rather than two assignment tokens.

### Supporting language versions

Keep the public `Lexer` interface stable and construct `PrintScriptLexer` with a version-specific rule set. Avoid spreading version checks through every rule when selecting a coherent group of rules can represent the version instead.

## Testing

Run the lexer tests from the repository root:

```shell
./gradlew :modules:lexer:test
```

The current integration tests cover:

- Operators and punctuation.
- Whitespace skipping.
- Original lexeme preservation.
- String literals.
- Decimal number literals.
- Variable declarations.
- Complete PrintScript programs.
- Unexpected-character errors and positions.
- Unterminated-string errors and positions.

Tests commonly materialize the token sequence because their inputs are small:

```kotlin
val tokens = lexer.tokenize(source.reader()).toList()
```

Useful additional test categories include:

- Empty input and whitespace-only input.
- Both string delimiters.
- Keyword prefixes such as `letter` and `printlnValue`.
- Identifiers containing underscores and digits.
- Integer, decimal, and invalid decimal boundaries.
- Token ranges before and after newlines.
- EOF position after trailing whitespace.
- One-shot sequence behavior.
- Large-reader tests that prove tokenization remains incremental.
- Rule-priority tests whenever matching prefixes overlap.

When testing errors from a lazy sequence, always consume far enough to encounter the problem. `assertFailsWith` around `tokenize` alone is insufficient.

## Current scope and limitations

The lexer currently implements the lexical elements required for the present PrintScript 1.0 subset. It intentionally does not implement:

- Comments.
- Escape sequences in strings.
- Multiline strings.
- Exponential or leading-dot numeric notation.
- Multi-character operators.
- Whitespace or comment tokens.
- Syntactic recovery after a lexical error.
- Parser, semantic, or runtime validation.

These are extension points rather than responsibilities that should be silently inferred by the current lexer.

## Design summary

The lexer follows several design principles:

- **Single responsibility:** the coordinator manages scanning, while each rule recognizes one token category.
- **Dependency inversion:** consumers can depend on `Lexer` rather than `PrintScriptLexer`.
- **Open extension:** new lexical categories can be introduced as rules without rewriting existing scanners.
- **Low memory usage:** source text flows through a reader and tokens are yielded lazily.
- **Immutable boundary objects:** tokens, positions, and ranges are immutable values shared across modules.
- **Precise diagnostics:** every token and lexical failure carries source location information.
- **Separation of phases:** lexical classification remains independent from parsing, semantic analysis, formatting policy, and execution.

The central invariant is simple: at every non-whitespace source position, exactly one rule must consume one complete token, or the lexer must report a precise lexical error.
