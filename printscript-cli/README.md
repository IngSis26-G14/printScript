# PrintScript CLI

Build the command-line distribution with Java 17:

```shell
./gradlew :printscript-cli:installDist
```

The executable is generated at:

```text
printscript-cli/build/install/printscript-cli/bin/printscript-cli
```

## Usage

```text
printscript <operation> <source> [options]
```

Examples:

```shell
printscript validation examples/program.ps --version 1.0
printscript execution examples/program.ps -v 1.0
printscript analyzing examples/program.ps --config examples/lint.json
printscript formatting examples/program.ps -v 1.1 --config examples/format.json
```

The version defaults to `1.0`. Versions `1.0` and `1.1` can be selected from
the terminal. The lexer, parser, and interpreter support both versions. Version 1.1
execution supports booleans, constants, conditionals, `readInput`, and `readEnv`.

Analysis configuration is a JSON object containing string, boolean, or integer
rule values:

```json
{
  "identifier_format": "camel case",
  "mandatory-variable-or-literal-in-println": true
}
```

Parsing progress and diagnostics are written to standard error. Program output
is written to standard output. Exit codes are `0` for success, `1` when a
diagnostic is found, `2` for invalid arguments, `3` for filesystem errors,
and `70` for an unexpected I/O failure.

Formatting writes source text to standard output, preserving exactly the newlines
produced by the formatter. The input file is not modified. Use a different output
file when redirecting the result. An empty JSON object selects the defaults.

Example `format.json` for version 1.1:

```json
{
  "enforce-spacing-before-colon-in-declaration": false,
  "enforce-spacing-after-colon-in-declaration": true,
  "enforce-spacing-around-equals": true,
  "line-breaks-before-println": 1,
  "indent-inside-if": 2
}
```

See the [formatter configuration](../printscript-formatter/README.md) for defaults,
mandatory rules, and the meaning of blank lines before `println`.
