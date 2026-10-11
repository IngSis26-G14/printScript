# PrintScript for IntelliJ IDEA

This plugin targets **IntelliJ IDEA 2026.2 (build 262)**. It provides:

- Warnings and errors directly in `.ps` files, with hover messages and source ranges.
- Analysis of the current document, including unsaved edits, without executing the program.
- Formatting warnings, including extra spaces around `=`.
- An **Alt+Enter → Format PrintScript file** quick fix on formatting warnings.
- A **Format PrintScript File** action in the editor context menu and Find Action.
- Undo support for formatting and protection against overwriting edits made while formatting runs.

## Build and install

From the repository root on this Mac:

```sh
python3 scripts/build-intellij-plugin.py
```

The script selects the installed JDK 17 for the language modules, runs the editor
module checks, compiles the plugin against the installed IDE using its bundled
Java 25+ compiler, runs SDK smoke tests, and packages:

```text
printscript-intellij/build/distributions/printscript-intellij-0.1.0.zip
```

For another installation:

```sh
python3 scripts/build-intellij-plugin.py --idea "/path/to/IntelliJ IDEA.app" --java-home "/path/to/jdk-17"
```

In IntelliJ:

1. Open **Settings → Plugins → gear menu → Install Plugin from Disk**.
2. Choose the ZIP above and restart if prompted.
3. Open `examples/program.ps` and add an extra space before `6`.
4. Wait for background analysis, then hover over the underlined spaces.
5. Press **Alt+Enter** (Option+Return on macOS) and select **Format PrintScript file**.
6. Undo the change with the usual Undo command to test it again.

If `.ps` is already assigned to another file type, select PrintScript for `*.ps`
in **Settings → Editor → File Types**. If no warnings appear, ensure highlighting
is set to **All Problems** and Power Save Mode is off.

## Configuration

The plugin searches the source directory and its ancestors, up to the project
root, for `lint.json` and `format.json`. The nearest file of each name wins.
This means `examples/program.ps` automatically uses `examples/lint.json` and
`examples/format.json` in this repository.

For explicit configuration, add `.printscript.properties` to the source directory
or an ancestor. Its paths are relative to that properties file:

```properties
version=1.1
lint.config=examples/lint.json
format.config=examples/format.json
```

The properties file stops the upward search. Omitted paths use JSON files found
at or below that directory, or the defaults below. Save configuration changes to
trigger a fresh analysis of open files. A referenced file that cannot be read
produces an error; it does not silently fall back to defaults.

Without configuration, editor defaults are:

- PrintScript **1.1** (set `version=1.0` explicitly to enforce 1.0).
- Camel-case identifier linting.
- A single space on either side of `=`.

A supplied JSON configuration replaces the defaults for that tool. For example,
`{}` disables formatting changes. To detect and fix the extra-space example:

```json
{
  "enforce-spacing-around-equals": true
}
```

Formatting warnings come from comparing the document with the existing
formatter's output. The CLI linter retains its existing set of rules. The editor
preserves a final newline when the input has one, avoiding an unrelated EOF
warning caused by the formatter's output convention.

## Development and verification

`printscript-editor` is a Gradle module containing the IDE-independent analysis
adapter and formatting comparison. It reuses the existing modules and JSON
configuration reader. `Diagnostic.span` exposes optional source ranges without
parsing terminal messages.

`printscript-intellij` contains the Java SDK adapter and plugin descriptor. The
build script uses the local IntelliJ SDK, so the normal Gradle build does not
need an IDE download or a new Gradle/JDK version. Rebuild the ZIP after source
changes and reinstall it to update the plugin. No installation is performed by
the build script.

```sh
export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
./gradlew check
python3 scripts/build-intellij-plugin.py
```

Tests cover exact lint and spacing ranges, formatting idempotence, configuration
errors, both language versions, syntax and semantic failures, Unicode, runtime
input without execution, annotation severities, quick-fix attachment, stale
analysis rejection, and EOF ranges. The SDK smoke tests exercise the real SDK
interfaces with test doubles; they do not launch an interactive IDE. Use the
installation steps above to verify the UI and Undo behavior.

This first version provides a minimal PSI shell for diagnostics. Syntax colors,
completion, rename refactoring, and IntelliJ's standard Reformat Code integration
are not implemented; use **Format PrintScript File**.
