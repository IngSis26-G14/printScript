# PrintScript Lexer

The lexer module performs lexical analysis for PrintScript. It reads source code as a
stream of characters and turns it into a stream of `Token` results that can be
consumed by the parser and, indirectly, by the interpreter, formatter, and static
analyzer.

The lexer answers questions such as:

- Is `let` a keyword or an identifier?
- Is `12.5` a number literal?
- Where in the source did a token begin and end?
- Is `"Hello"` a valid string literal?
- Does an input character belong to any valid PrintScript token?

It does not decide whether a sequence of valid tokens forms a valid program. Syntax,
type rules, variable declarations, operator precedence, and evaluation belong to
later stages of the language pipeline.

## Language pipeline

```text
Sequence<Char>
  |
  v
Lexer  -- Sequence<Outcome<Token, Diagnostic>> -->  Parser  -->  AST  -->  Interpreter
                                                    |   |
                                                    |   +-----> Formatter
                                                    +---------> Static analyzer
```

The lexer is the only stage concerned with grouping raw characters into lexical
units. For example:

```printscript
println("Hello" + 12.5);
```

is emitted approximately as:

| Token type       | Lexeme    |
| ---------------- | --------- |
| `PRINTLN`        | `println` |
| `LEFT_PARENTHESIS` | `(`     |
| `STRING_LITERAL` | `"Hello"` |
| `ADDITION_OPERATOR`| `+`       |
| `NUMBER_LITERAL` | `12.5`    |
| `RIGHT_PARENTHESIS` | `)`    |
| `SEMICOLON`      | `;`       |

Each row represents one successfully produced `Token`, wrapped in `Outcome.Ok`. A
literal is not emitted as separate "value" and "type" tokens. Instead, the same
token contains its classification, original lexeme, and source span.

## Module boundary

The module is declared in Gradle as `:modules:lexer` and depends on `:modules:common`
and `:modules:api`:

```text
lexer --> common
lexer --> api
```

The common module supplies the shared data structures used at module boundaries:

- `common.model.token.Token`
- `common.model.token.TokenType`
- `common.model.trivia.Trivia`
- `common.model.trivia.TriviaType`
- `common.model.span.Position`
- `common.model.span.Span`
- `common.model.diagnostic.Diagnostic`
- `common.type.outcome.Outcome`
- `common.type.option.Option`

The `api` module supplies the public contract the lexer implements:

- `api.Lexer`

The lexer module exposes `PrintScriptLexer`. Its cursor, scanner, trivia collector,
rule table, and individual lexical rules are `internal` implementation details.

The parser should depend on the `Lexer` interface from `api` when possible, rather
than on implementation details such as `SourceCursor`, `TokenScanner`, or a
particular rule class.

## Build and tooling

The repository configures the project with:

- Kotlin/JVM 2.2.0.
- Java 21 through Gradle's JVM toolchain.
- Maven Central for dependency resolution.
- `kotlin-test` for automated tests running on the JUnit Platform.
- Kover for code-coverage reporting and verification.

The lexer has no external runtime library dependency beyond the project's `common`
and `api` modules. The root Kover configuration aggregates the project modules and
currently declares an 80 percent minimum coverage bound.

## Source layout

```text
modules/lexer/
├── build.gradle
├── README.md
└── src/
    ├── main/kotlin/lexer/
    │   ├── PrintScriptLexer.kt
    │   ├── SourceCursor.kt
    │   ├── rules/
    │   │   ├── LexerRule.kt
    │   │   ├── NumberRule.kt
    │   │   ├── StringRule.kt
    │   │   ├── SymbolRule.kt
    │   │   └── WordRule.kt
    │   └── internal/
    │       ├── buffer/            (not currently used — SourceCursor plays this role)
    │       ├── collector/
    │       │   └── TriviaCollector.kt
    │       ├── model/
    │       │   ├── category/
    │       │   │   └── Lexical.kt
    │       │   ├── error/
    │       │   │   ├── ConfigurationError.kt
    │       │   │   └── LexError.kt
    │       │   └── scan/
    │       │       └── TokenScan.kt
    │       ├── scanner/
    │       │   └── TokenScanner.kt
    │       └── table/
    │           ├── RuleTable.kt
    │           └── RuleTableRegistry.kt
    └── test/kotlin/
        └── PrintScriptLexerTest.kt
```

## Public API

### `Lexer` (defined in `api`)

`Lexer` is the abstraction exposed to consumers:

```kotlin
interface Lexer {
    fun lex(version: String, chars: Sequence<Char>): Sequence<Outcome<Token, Diagnostic>>
}
```

It accepts a `Sequence<Char>` so callers can supply source code from a file, a
string, or any other character source without changing the lexer, and without
coupling the contract to a JVM-specific I/O type such as `Reader`. It accepts a
`version` string because future PrintScript versions may introduce lexical
differences (new keywords, comment syntax, escape sequences); the current
implementation only registers a rule table for `"1.0"`.

Errors are values, not exceptions: each element of the returned sequence is either
`Outcome.Ok(token)` or `Outcome.Error(diagnostic)`. The sequence stops after the
first error — the lexer does not attempt error recovery.

### `PrintScriptLexer`

`PrintScriptLexer` is the PrintScript implementation of `Lexer`. Its public
no-argument constructor installs the standard collaborators (`TokenScanner`,
`TriviaCollector`), which in turn use the rule table resolved for the requested
version. The `"1.0"` table installs, in priority order:

1. `StringRule`
2. `NumberRule`
3. `WordRule`
4. `SymbolRule`

Its primary constructor is internal and accepts the scanner and trivia collector
directly. This permits module tests and future version-specific construction
without exposing rule implementations as public API.

### Basic usage

```kotlin
val lexer: Lexer = PrintScriptLexer()

val outcomes = lexer.lex("1.0", "let result: number = 12.5;".asSequence())

outcomes.forEach { outcome ->
    when (outcome) {
        is Outcome.Ok -> println("${outcome.value.type}: ${outcome.value.lexeme}")
        is Outcome.Error -> println("Lexical error: ${outcome.error.format()}")
    }
}
```

For a file, read it into a lazy character sequence while the source remains
available:

```kotlin
file.bufferedReader().use { reader ->
    val chars = reader.lineSequence().joinToString("\n").asSequence()
    val outcomes = lexer.lex("1.0", chars)
    parser.parse("1.0", outcomes.filterIsOkTokens())
}
```

The lexer does not own or close any underlying resource; resource ownership
remains with the caller.

## Streaming and memory behavior

Tokenization is lazy. `lex` returns a sequence created with Kotlin's `sequence`
builder, and the lexer does not scan the complete source until a consumer requests
tokens.

The implementation constrains the returned sequence to one iteration:

```kotlin
sequence {
    // Read and yield token outcomes incrementally.
}.constrainOnce()
```

Consequences of this design:

- Creating the sequence does not necessarily read or validate the source.
- A lexical error is represented as the final `Outcome.Error` element rather than
  a thrown exception; consumers must inspect elements as they are produced.
- Iterating the same outcome sequence twice is not supported.
- Production consumers should process outcomes incrementally instead of calling
  `toList()` on a very large source.
- Using `toList()` in unit tests is appropriate because test inputs are
  intentionally small.

`SourceCursor` wraps the character sequence and maintains only the lookahead
required by lexical rules (a small `ArrayDeque<Char>`). The complete source is
never copied into one string. Memory use is therefore bounded by lookahead and the
size of the current token rather than by the size of the complete program.

## Token model

A token contains five fields:

```kotlin
data class Token(
    val type: TokenType,
    val lexeme: String,
    val span: Span,
    val leading: Collection<Trivia> = emptyList(),
    val trailing: Collection<Trivia> = emptyList(),
)
```

### Type

`type` describes the lexical category. The parser normally makes decisions based on
this field.

### Lexeme

`lexeme` is the exact text consumed from the source. Examples include:

```text
Token(TokenType.LET, "let", ...)
Token(TokenType.IDENTIFIER, "result", ...)
Token(TokenType.NUMBER_LITERAL, "12.5", ...)
Token(TokenType.STRING_LITERAL, "'Hello'", ...)
Token(TokenType.ADDITION_OPERATOR, "+", ...)
```

String lexemes retain their opening and closing quotation marks. The parser can
later turn `'Hello'` or `"Hello"` into an AST string value containing `Hello`.
Similarly, the parser can convert a number lexeme such as `12.5` into its chosen
numeric representation.

Keeping the raw lexeme is useful for diagnostics, formatting, source
reconstruction, and deferring value conversion to the correct compiler stage.

### Span

`span` identifies where the lexeme appeared. Positions contain:

- `line`: one-based line number.
- `column`: one-based column number.
- `index`: zero-based character offset from the start of the source.

Spans are end-exclusive. For the token `let` at the beginning of a file:

```text
start = Position(line = 1, column = 1, index = 0)
end   = Position(line = 1, column = 4, index = 3)
```

The end position points immediately after the token. This convention makes spans
composable and makes token length equal to `end.index - start.index`.

Only `\n` advances the cursor to a new line. Other consumed characters advance the
column.

### Trivia

`leading` and `trailing` hold whitespace (and, once supported, comments) adjacent
to the token. Unlike the previous design, whitespace is not discarded: it is
collected by `TriviaCollector` immediately before each token and attached to that
token's `leading` collection. This preserves the information a future formatter
needs to make spacing decisions, without requiring the parser to look at it — the
parser ignores trivia entirely when applying grammar rules.

`trailing` is currently always empty; the lexer only attaches leading trivia. This
is a deliberate simplification for the current scope, not a limitation of the
`Token` model itself.

## Supported token types

### Keywords

| Source    | Token type |
| --------- | ---------- |
| `let`     | `LET`      |
| `println` | `PRINTLN`  |

### Type names

| Source   | Token type    |
| -------- | ------------- |
| `number` | ` TYPE_NUMBER`|
| `string` | `TYPE_STRING` |

### Identifiers

An identifier starts with a letter or underscore. Remaining characters may be
letters, digits, or underscores.

Examples:

```text
name
lastName
result2
_temporary
```

The complete word is consumed before keyword classification. Therefore `let` is a
keyword, while `letter` and `let2` are identifiers.

The current implementation uses Kotlin's `isLetter` and `isLetterOrDigit`, so it
accepts Unicode letters and digits. If the language is later restricted to ASCII
identifiers, the rule should use explicit `a..z`, `A..Z`, and `0..9` checks.

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

The decimal point is consumed only when at least one digit follows it. As a
result:

- `12.` becomes `NUMBER_LITERAL("12")` followed by an unexpected `.` error.
- `.5` begins with an unexpected `.` error.
- Exponential notation such as `1e10` is not implemented as a single number
  literal.
- A leading sign is not part of the literal.

For example, `-5` is emitted as:

```text
Token(TokenType.MINUS, "-")
Token(TokenType.NUMBER_LITERAL, "5")
```

The parser determines whether `-` represents subtraction or unary negation. The
lexer must not make that syntactic decision.

### String literals

Strings may use matching single or double quotes:

```printscript
'Hello'
"Hello"
```

Both forms produce `STRING_LITERAL`. Their raw lexemes retain the delimiters, and
the closing delimiter must match the opening one.

A string ends at the next occurrence of its opening delimiter. Reaching EOF or a
newline first produces `Outcome.Error(LexError(...))` for an unterminated string,
reported at the position of the *opening* delimiter. Multiline strings are
therefore not supported.

Escape-sequence handling is not currently implemented. Syntax such as
`"a \"quoted\" value"` requires an explicit language decision and corresponding
changes in both the lexer and parser before it can be supported reliably.

### Operators and delimiters

| Source | Token type          |
| ------ | -------------------- |
| `=`    | `ASSIGNMENT_OPERATOR` |
| `+`    | `ASSIGNMENT_OPERATOR`|
| `-`    | `ASSIGNMENT_OPERATOR`|
| `*`    | `ASSIGNMENT_OPERATOR`|
| `/`    | `ASSIGNMENT_OPERATOR`|
| `:`    | `COLON`               |
| `;`    | `SEMICOLON`           |
| `(`    | `LEFT_PARENTHESIS`    |
| `)`    | `RIGHT_PARENTHESIS`   |

All currently supported symbols consist of one character. Multi-character
operators would require longest-match behavior so that, for example, `==` is
considered before `=`.

### End of source

Unlike an earlier version of this design, the lexer does **not** emit an explicit
`EOF` token. The returned sequence simply ends once the source is exhausted, and
downstream consumers (`TokenBuffer` in the parser) determine the end of input with
`hasNext()` rather than by matching a sentinel token. An empty source produces an
empty sequence.

## Rule architecture

Every lexical category implements:

```kotlin
internal interface LexerRule {
    fun matches(cursor: SourceCursor): Boolean
    fun read(cursor: SourceCursor): Outcome<Token, Diagnostic>
}
```

The rule contract is:

1. `matches` may inspect the cursor but must not consume input.
2. `read` is called only when `matches` is true.
3. `read` consumes exactly one complete token, or fails without leaving the cursor
   in a partially-consumed, ambiguous state.
4. `read` returns `Outcome.Ok(token)` whose lexeme and span correspond to the
   consumed characters, or `Outcome.Error(diagnostic)` when the input is lexically
   invalid (for example, an unterminated string).
5. Invalid user input produces a lexical `Diagnostic`, returned as an `Outcome`
   value; a violated rule precondition (calling `read` when `matches` is false, or
   a rule that consumes zero characters) indicates an implementation error and
   fails fast with an exception, since it is not something user source code can
   trigger.

`TokenScanner` selects the first matching rule for the current cursor position,
delegates to it, and translates the result into a `TokenScan` (`Ok`, `Error`, or
`Empty`). After a successful read it verifies that the cursor offset advanced.
This protects the lazy loop from a faulty rule that returns a token without
consuming input.

Whitespace is intentionally not a `LexerRule` implementation: it is collected by
`TriviaCollector` before each scan attempt and attached to the next token as
leading trivia, rather than being matched, consumed, and discarded by a rule like
the others.

### Rule priority

The current rules have non-overlapping starting characters, but rule order becomes
important when the language grows. For example, a future `//` comment rule and the
current division rule both begin with `/`. The comment rule would need to appear
before `SymbolRule` in the rule table, or symbol matching would consume `/` first.

Priority should always be explicit (encoded as list order in the rule table) and
covered by tests when two rules can match the same prefix.

## Cursor behavior

`SourceCursor` centralizes all interaction with the character sequence. Rules must
use `peek` and `advance` rather than reading directly.

Its operations are:

- `peek(distance = 0)`: inspect a character without consuming it, optionally ahead
  of the current position.
- `advance()`: consume one character and update offset, line, and column.
- `isAtEnd()`: report whether no character remains.
- `currentPosition()`: capture an immutable `Position`.

Centralizing movement guarantees that all rules update positions consistently. It
also allows the lookahead strategy to remain independent of rule logic.

## Tokenization algorithm

For each requested token, `PrintScriptLexer` performs the following process:

```text
resolve rule table for `version`
      |
      +---- unsupported version ----> emit ConfigurationError and finish
      |
      v
collect leading trivia
      |
      v
at end of input? ---- yes ----> finish (no token emitted)
      |
      no
      |
      v
TokenScanner finds first matching rule
      |
      +---- none matches ----> emit LexError("Unexpected character") and finish
      |
      v
rule reads one token
      |
      +---- rule reports failure ----> emit that Diagnostic and finish
      |
      v
verify cursor advanced
      |
      v
attach leading trivia, yield Outcome.Ok(token), repeat
```

With a fixed, small rule set, scanning is linear in the number of source
characters. The rule-selection cost is proportional to the number of registered
rules, which is currently four.

## Lexical errors

Lexical errors are represented as `Diagnostic` values — `LexError` (category
`Lexical`, carrying a `Span`) for invalid source, and `ConfigurationError` for an
unsupported `version` argument. Neither extends `Exception`; both are returned
through `Outcome.Error`, consistent with how the parser reports `ParseError`.

### Unexpected characters

If no rule matches the current non-whitespace character, the lexer yields:

```text
Outcome.Error(LexError("Unexpected character '@'", span, Lexical))
```

### Unterminated strings

If a string reaches a newline or the end of input before its closing delimiter,
`StringRule` returns:

```text
Outcome.Error(LexError("Unterminated string starting at line 1, column 1", span, Lexical))
```

The stored span starts at the position of the opening quote.

Because tokenization is lazy, tests and consumers must request enough elements to
reach the invalid input:

```kotlin
val outcomes = lexer.lex("1.0", "let @".asSequence()).toList()
val error = outcomes.last() as Outcome.Error
```

Calling only `lexer.lex(...)` constructs a sequence and does not guarantee that
validation has occurred.

The assignment requires diagnostics to identify both the beginning and end of a
problem. `Span` (rather than a single `Position`) satisfies this from the start,
so no later migration is needed here.

## Interaction with the parser

The lexer reports lexical facts; the parser assigns grammatical meaning.

For example, the lexer emits:

```text
Outcome.Ok(Token(TokenType.NUMBER_LITERAL, "12.5", span))
Outcome.Ok(Token(TokenType.STRING_LITERAL, "'Hello'", span))
```

The parser can convert those tokens into AST values:

```text
NumberLiteral(value = 12.5)
StringLiteral(value = "Hello")
```

Value conversion should not destroy the original token information if later tools
need precise diagnostics or formatting. Decimal representation is also a
parser/interpreter design decision; `BigDecimal` is often preferable when exact
decimal arithmetic is required.

The parser, not the lexer, is responsible for detecting conditions such as:

- Missing semicolons.
- Invalid declaration structure.
- Unbalanced parentheses.
- Invalid expression order.
- Operator precedence.
- Assignment to undeclared variables.
- Type incompatibility.
- Incorrect `println` arguments.

For example, `let value: number = "text";` is lexically valid even though a later
semantic stage should reject the type mismatch.

## Interaction with other tools

### Interpreter

The interpreter should operate on the AST, not directly on raw tokens. It
evaluates already parsed literal values, expressions, declarations, assignments,
and `println` calls.

### Formatter

Because whitespace is now preserved as trivia rather than discarded, a formatter
can either respect or override it: reading `leading`/`trailing` gives it access to
the whitespace actually present in the source, while its own spacing rules
(configured separately, similar to how `GrammarTable` is chosen per version)
determine what to emit. If exact lossless source round-tripping is required in the
future, comments will also need to be represented in the trivia model.

### Static analyzer

The analyzer works primarily from the AST and symbol/type information. Token and
AST spans allow findings to refer back to precise locations in the original file.

### CLI and progress reporting

The CLI should keep the character source available while parsing and can update
progress as tokens or AST nodes are consumed. The lexer exposes offsets suitable
for downstream progress calculations, but presenting progress is not a lexer
responsibility.

## Extending the lexer

### Adding a keyword

1. Add the new entry to `TokenType` in the common module.
2. Add its spelling to `WordRule.reservedWords`.
3. Add tests proving the exact word is a keyword and longer words remain
   identifiers.

For example, adding `const` must not cause `constant` to become `CONST` plus
`IDENTIFIER`.

### Adding a one-character symbol

1. Add its `TokenType`.
2. Add the character-to-type mapping in `SymbolRule`.
3. Add lexer tests for its type, lexeme, and span.

### Adding a new lexical category

1. Define any required token types in `common`.
2. Implement a stateless `LexerRule` returning `Outcome<Token, Diagnostic>`.
3. Add the rule to the appropriate version's rule table (`RuleTableRegistry`) at
   the correct priority.
4. Test `matches`, consumption boundaries, lexeme preservation, spans, and invalid
   input.
5. Add an integration test involving surrounding token categories.

### Adding comments

First decide whether comments should be discarded or preserved as trivia (the
`Trivia`/`TriviaType` model already supports a comment variant). A `//` rule must
take priority over `/` as division. Block comments also need a defined
unterminated-comment error and correct multiline position tracking.

### Adding multi-character operators

Use maximal munch: inspect all operators sharing the prefix and consume the
longest valid one. For a language supporting both `=` and `==`, the input `==`
must become one equality token rather than two assignment tokens.

### Supporting language versions

Keep the public `Lexer` interface stable (it already accepts `version`) and
register a new `RuleTable` in `RuleTableRegistry` for the new version string.
Avoid spreading version checks through every rule when selecting a coherent group
of rules can represent the version instead — this mirrors how the parser resolves
a `GrammarTable` per version.

## Testing

Run the lexer tests from the repository root:

```shell
./gradlew :modules:lexer:test
```

The current integration tests cover:

- Operators and punctuation.
- Whitespace collection as leading trivia.
- Original lexeme preservation.
- String literals.
- Decimal number literals.
- Variable declarations.
- Complete PrintScript programs.
- Unexpected-character diagnostics and their spans.
- Unterminated-string diagnostics and their spans.

Tests commonly materialize the outcome sequence because their inputs are small:

```kotlin
val outcomes = lexer.lex("1.0", source.asSequence()).toList()
```

Useful additional test categories include:

- Empty input and whitespace-only input.
- Both string delimiters.
- Keyword prefixes such as `letter` and `printlnValue`.
- Identifiers containing underscores and digits.
- Integer, decimal, and invalid decimal boundaries.
- Token spans before and after newlines.
- One-shot sequence behavior.
- Large-source tests that prove tokenization remains incremental.
- Rule-priority tests whenever matching prefixes overlap.
- Trivia attachment tests (that the right whitespace ends up as `leading` on the
  right token).

When testing errors from a lazy sequence, always consume far enough to encounter
the problem. Asserting on the sequence's construction alone is insufficient — the
error is the last produced element, not a thrown exception.

## Current scope and limitations

The lexer currently implements the lexical elements required for the present
PrintScript 1.0 subset. It intentionally does not implement:

- Comments.
- Escape sequences in strings.
- Multiline strings.
- Exponential or leading-dot numeric notation.
- Multi-character operators.
- Trailing trivia (only leading trivia is currently attached).
- Syntactic recovery after a lexical error.
- Parser, semantic, or runtime validation.

These are extension points rather than responsibilities that should be silently
inferred by the current lexer.

## Design summary

The lexer follows several design principles:

- **Single responsibility:** the coordinator manages scanning, while each rule
  recognizes one token category.
- **Dependency inversion:** consumers depend on `Lexer` (from `api`) rather than
  on `PrintScriptLexer` or its internal collaborators.
- **Open extension:** new lexical categories can be introduced as rules, and new
  language versions as rule tables, without rewriting existing scanners.
- **Low memory usage:** source text flows through a lazy `Sequence<Char>` and
  token outcomes are yielded lazily.
- **Errors as values:** lexical failures are `Diagnostic`s delivered through
  `Outcome`, not exceptions — consistent with how the parser reports syntax
  errors.
- **Immutable boundary objects:** tokens, positions, spans, and trivia are
  immutable values shared across modules.
- **Precise diagnostics:** every token and lexical failure carries a source span.
- **Separation of phases:** lexical classification remains independent from
  parsing, semantic analysis, formatting policy, and execution.

The central invariant is simple: at every source position, exactly one rule must
consume one complete token (with its preceding trivia already collected), or the
lexer must report a precise lexical diagnostic.