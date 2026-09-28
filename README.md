# PrintScript

Kotlin implementation of PrintScript 1.0 and 1.1, with a lexer, parser, semantic
validator, interpreter, formatter, linter, and command-line application.

## Build and run

Use **JDK 17** and the included Gradle wrapper:

```shell
./gradlew check
./gradlew :printscript-cli:installDist
printscript-cli/build/install/printscript-cli/bin/printscript-cli execution examples/v1.1.ps -v 1.1
```

The CLI supports `validation`, `execution`, `formatting`, and `analyzing`.
Formatting writes to standard output; it does not overwrite the source file.
See the [CLI guide](printscript-cli/README.md) for arguments and exit codes.

## Language and modules

Version 1.0 supports numbers, strings, declarations, assignments, arithmetic,
and `println`. Version 1.1 adds booleans, constants, `if`/`else`, `readInput`,
and `readEnv`. Conditions have the form `if (identifier) { ... }`; validation
requires a declared, initialized boolean variable.

- [Lexer](printscript-lexer/README.md): tokenization and language version rules.
- [Parser](printscript-parser/README.md): grammar, AST, and streaming token cursor.
- [Validator](printscript-validator/README.md): declarations, types, and initialization.
- [Interpreter](printscript-interpreter/README.md): evaluation and runtime I/O.
- [Formatter](printscript-formatter/README.md): defaults and JSON formatting rules.
- [Linter](printscript-linter/README.md): configurable style diagnostics.

The pipeline consumes source incrementally. The parser buffers the current
statement or block, with no fixed token ceiling; a single very large block still
requires memory for its tokens and AST.

## Git hooks

Install the pre-commit hook once per clone:

```shell
/bin/sh scripts/install-hooks.sh
```

From another directory, pass the absolute path to the installer. It locates the
repository from its own path, makes `.githooks/pre-commit` executable, and sets
local `core.hooksPath` to `.githooks`. Repeating installation is safe.

The hook runs `./gradlew check` from the repository root and aborts the commit
when checks fail. On macOS, the hook selects an installed JDK 17 using
`/usr/libexec/java_home -v 17`, overriding any JDK inherited from an IDE.
On other systems, make JDK 17 available through `JAVA_HOME` or `PATH`.
It checks the working tree, so stage the final changes before committing.

Test hook installation and commit blocking without changing this repository's
history:

```shell
/bin/sh scripts/test-hooks.sh
```

The test uses a temporary repository (including spaces in its path) and a fake
Gradle wrapper to verify successful and failed checks through actual Git commits.
CI runs this test as well as the real Gradle checks.

## Tests and coverage

`./gradlew check` runs tests, Detekt, Spotless, and the configured merged Kover
coverage verification. The current merged minimum is **75%**, covering the lexer,
parser, interpreter, and CLI. API, common, formatter, validator, and linter are
excluded from that merged gate; per-module coverage gates are not configured.
Formatter and validator tests still run as part of `check`.

Regression tests cover both language versions, diagnostics, runtime input and
environment conversion, nested conditionals, long statements and large files,
formatter configuration and idempotence, and CLI integration. The CLI tests also
verify that formatted source validates and preserves execution in both branches,
and that nested lint rules work without performing runtime input.

The merged HTML report is at `build/reports/kover/merged/html/index.html`.
Run `./gradlew koverModulesHtmlReport` to generate module reports and a dashboard
at `build/reports/kover/modules/index.html`. Dashboard comparisons use the same
75% target for display; they do not add per-module build gates.
