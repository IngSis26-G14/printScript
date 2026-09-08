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
```

The version defaults to `1.0`. Versions `1.0` and `1.1` can be selected from
the terminal, although the lexer, parser, and interpreter still need language
support for `1.1` before that version can process programs.

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
diagnostic is found, `2` for invalid arguments, `3` for filesystem errors, `4`
for an unavailable operation, and `70` for an unexpected I/O failure.

Formatting is recognized by the CLI but is currently unavailable because this
repository does not contain a formatter implementation.
