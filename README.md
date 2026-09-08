# printScript
This will the repository responsible of the lexer, parser , interpreter, linter, formatter. This will also include CI / CD pipelines. 

## Code coverage

Kover is applied to every module through the shared build convention and is
also declared by each module. Both per-module and merged line coverage require
a minimum of 80%.

Run all tests and coverage checks with Java 17:

```shell
./gradlew check
```

The merged HTML report is generated at:

```text
build/reports/kover/merged/html/index.html
```

An individual module report is generated under that module's
`build/reports/kover/html` directory.

To generate a single dashboard that lists all modules and links to each
module's detailed report, run the `koverModulesHtmlReport` Gradle task. Its
output is generated at:

```text
build/reports/kover/modules/index.html
```
