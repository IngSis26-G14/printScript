# PrintScript Formatter

`PrintScriptFormatter.format(version, nodes, rules)` formats a sequence of AST
nodes for PrintScript 1.0 or 1.1, yielding one `Doc` per top-level node. It retains
the current subtree while formatting; it does not collect the entire program.
`FormatterRunner` connects source input, lexer, parser, config, diagnostics, and
an output writer. The CLI uses JSON configuration and writes formatted text to
standard output.

## Configuration

An empty configuration uses these defaults:

| JSON key | Values | Default | Version |
| --- | --- | --- | --- |
| `enforce-spacing-before-colon-in-declaration` | boolean | `false` | 1.0, 1.1 |
| `enforce-spacing-after-colon-in-declaration` | boolean | `true` | 1.0, 1.1 |
| `enforce-spacing-around-equals` | boolean | `true` | 1.0, 1.1 |
| `line-breaks-before-println` | 0, 1, 2 | 0 | 1.0, 1.1 |
| `indent-inside-if` | nonnegative integer | 4 | 1.1 |

`false` for a spacing option removes that space. `line-breaks-before-println`
specifies additional blank lines before each `println`, beyond the mandatory
line break after a preceding statement or opening brace. For example, `1` yields:

```printscript
let x: number = 1;

println(x);
```

It also adds the requested leading newlines when `println` is the first statement.
Blank lines contain no indentation; nonempty block lines have the configured
indentation. A final statement always retains its terminating newline.

The legacy `enforce-no-spacing-around-equals` option is supported as the inverse
of `enforce-spacing-around-equals`; contradictory settings produce a configuration
diagnostic. The former `line-breaks-after-println` key is replaced by
`line-breaks-before-println`. Unknown keys, unsupported version-specific rules,
wrong value types, duplicate rules, and out-of-range values produce diagnostics.

## Mandatory formatting

The formatter always normalizes whitespace between tokens, places spaces around
arithmetic operators, and inserts a newline after every semicolon. In 1.1, opening
braces stay on the same line as `if`/`else`. These rules apply even with `{}` as the
configuration; explicitly disabling them is rejected. Literal string contents
are preserved. Operator spacing also applies to unary `+` and `-`.

For compatibility, the mandatory keys `mandatory-single-space-separation`,
`mandatory-space-surrounding-operations`, `mandatory-line-break-after-statement`,
and `if-brace-same-line` can be supplied with `true`. The option
`if-brace-below-line` is not supported because it conflicts with the required
brace layout.

## Rule traversal

Version tables install default and configured visitors in a fixed order, so JSON
property order cannot alter the result. Each visitor runs once per node in a
bottom-up pass. `NodeTransformer` preserves changed composite children and passes
visitor context between nodes. Visitors return `NodeValue`; conversion to `Doc`
happens after every rule has run. Formatting the result again produces the same
text.

Tests cover defaults, all spacing combinations, nested blocks, blank lines,
literal preservation, idempotence, invalid configuration, and CLI output.

```shell
./gradlew :printscript-formatter:test :printscript-cli:test
```
