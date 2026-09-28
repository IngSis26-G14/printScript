# PrintScript Formatter

`PrintScriptFormatter.format(version, nodes, rules)` formats a sequence of AST
nodes for PrintScript 1.0 or 1.1 and yields one `Doc` per top-level node.
`FormatterRunner` connects source input, lexer, parser, configuration,
diagnostics, and an output writer.

## Configuration

Only rules present in the configuration are applied. An empty configuration
preserves the source formatting. Rule execution uses a stable order, independent
of JSON property order.

| JSON key | Values | Version |
| --- | --- | --- |
| `enforce-no-spacing-around-equals` | boolean | 1.0, 1.1 |
| `enforce-spacing-around-equals` | boolean | 1.0, 1.1 |
| `enforce-spacing-before-colon-in-declaration` | boolean | 1.0, 1.1 |
| `enforce-spacing-after-colon-in-declaration` | boolean | 1.0, 1.1 |
| `mandatory-single-space-separation` | `true` | 1.0, 1.1 |
| `mandatory-space-surrounding-operations` | `true` | 1.0, 1.1 |
| `mandatory-line-break-after-statement` | `true` | 1.0, 1.1 |
| `line-breaks-after-println` | 0, 1, 2 | 1.0, 1.1 |
| `line-breaks-before-println` | 0, 1, 2 | 1.0, 1.1 |
| `indent-inside-if` | nonnegative integer | 1.1 |
| `if-brace-same-line` | `true` | 1.1 |
| `if-brace-below-line` | boolean | 1.1 |

`line-breaks-after-println` is the TCK rule and adds the requested blank lines
after each `println`. `line-breaks-before-println` remains available for clients
that used the newer CLI spelling and adds blank lines before each `println`.

The two equals-spacing keys are inverse forms. Contradictory values produce a
configuration diagnostic. Unknown rules, rules unavailable in the selected
language version, invalid value types, duplicate rules, and out-of-range values
also produce diagnostics.

## AST compatibility

Brace rules support both parser-produced `BlockNode` trees and the flatter AST
shape used by the formatter TCK. Formatting retains the current subtree and does
not need to collect the entire program.

```shell
./gradlew :printscript-formatter:test :printscript-cli:test
```
