import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import java.util.Locale

plugins {
    id("base")
    id("org.jetbrains.kotlinx.kover")
    id("co.uzzu.dotenv.gradle") version "4.0.0"
}

repositories {
    mavenCentral()
}

val minimumCoverage = 75

koverMerged {
    enable()
    htmlReport { onCheck = true }
    verify { rule { bound { minValue = minimumCoverage } } }
    filters {
        projects {
            excludes += listOf(":printscript-api", ":printscript-common",":printscript-formatter",":printscript-validator",":printscript-linter",)
        }
    }
}

tasks.named("check") {
    dependsOn("koverMergedVerify")
}

val modulesCoverageDashboard = layout.buildDirectory.file(
    "reports/kover/modules/index.html",
)

tasks.register("koverModulesHtmlReport") {
    group = "verification"
    description = "Generates a coverage dashboard with links to every module report."

    dependsOn("koverMergedHtmlReport", "koverMergedXmlReport")
    dependsOn(
        subprojects.flatMap { project ->
            listOf(
                "${project.path}:koverHtmlReport",
                "${project.path}:koverXmlReport",
            )
        },
    )

    inputs.files(
        subprojects.map { project ->
            project.layout.buildDirectory.file("reports/kover/xml/report.xml")
        },
    )
    inputs.file(layout.buildDirectory.file("reports/kover/merged/xml/report.xml"))
    inputs.property("minimumCoverage", minimumCoverage)
    outputs.file(modulesCoverageDashboard)

    doLast {
        data class CoverageResult(
            val covered: Int,
            val missed: Int,
        ) {
            val percentage: Double
                get() = if (covered + missed == 0) {
                    100.0
                } else {
                    covered * 100.0 / (covered + missed)
                }
        }

        fun readCoverage(reportFile: File): CoverageResult? {
            if (!reportFile.isFile) return null

            val lineCounter = Regex(
                """<counter type="LINE" missed="(\d+)" covered="(\d+)"\s*/>""",
            ).findAll(reportFile.readText()).lastOrNull() ?: return null

            return CoverageResult(
                missed = lineCounter.groupValues[1].toInt(),
                covered = lineCounter.groupValues[2].toInt(),
            )
        }

        fun percentage(value: Double): String =
            String.format(Locale.US, "%.2f%%", value)

        val rows = subprojects.sortedBy { it.name }.joinToString("\n") { project ->
            val xmlReport = project.layout.buildDirectory
                .file("reports/kover/xml/report.xml")
                .get()
                .asFile
            val htmlReport = project.layout.buildDirectory
                .file("reports/kover/html/index.html")
                .get()
                .asFile
            val coverage = readCoverage(xmlReport)

            if (coverage == null || !htmlReport.isFile) {
                """
                <tr>
                  <td>${project.name}</td>
                  <td class="unavailable">No test data</td>
                  <td class="unavailable">Not evaluated</td>
                  <td>Generate tests to create a report</td>
                </tr>
                """.trimIndent()
            } else {
                val passes = coverage.percentage >= minimumCoverage
                val status = if (passes) "Pass" else "Fail"
                val statusClass = if (passes) "pass" else "fail"
                val relativeReport =
                    "../../../../${project.name}/build/reports/kover/html/index.html"

                """
                <tr>
                  <td>${project.name}</td>
                  <td>${percentage(coverage.percentage)}</td>
                  <td class="$statusClass">$status</td>
                  <td><a href="$relativeReport">Open detailed report</a></td>
                </tr>
                """.trimIndent()
            }
        }

        val mergedXmlReport = layout.buildDirectory
            .file("reports/kover/merged/xml/report.xml")
            .get()
            .asFile
        val mergedCoverage = readCoverage(mergedXmlReport)
        val mergedSummary = mergedCoverage?.let { coverage ->
            val passes = coverage.percentage >= minimumCoverage
            val status = if (passes) "Pass" else "Fail"
            val statusClass = if (passes) "pass" else "fail"
            """Merged coverage: <strong>${percentage(coverage.percentage)}</strong>
                <span class="$statusClass">$status</span>"""
        } ?: "Merged coverage is unavailable."

        val outputFile = modulesCoverageDashboard.get().asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>PrintScript module coverage</title>
              <style>
                :root { color-scheme: light dark; font-family: system-ui, sans-serif; }
                body { max-width: 1100px; margin: 3rem auto; padding: 0 1.5rem; }
                table { width: 100%; border-collapse: collapse; margin-top: 2rem; }
                th, td { border-bottom: 1px solid #8886; padding: 0.8rem; text-align: left; }
                th { background: #8882; }
                .pass { color: #16803c; font-weight: 700; }
                .fail { color: #c42b1c; font-weight: 700; }
                .unavailable { color: #777; }
                a { color: #1677c8; }
              </style>
            </head>
            <body>
              <h1>PrintScript module coverage</h1>
              <p>Required line coverage: <strong>$minimumCoverage%</strong></p>
              <p>$mergedSummary</p>
              <p><a href="../merged/html/index.html">Open native merged Kover report</a></p>
              <table>
                <thead>
                  <tr>
                    <th>Module</th>
                    <th>Line coverage</th>
                    <th>Status</th>
                    <th>Details</th>
                  </tr>
                </thead>
                <tbody>
${rows.prependIndent("                  ")}
                </tbody>
              </table>
            </body>
            </html>
            """.trimIndent(),
        )

        logger.lifecycle("Module coverage dashboard: ${outputFile.toURI()}")
    }
}

val publishedProjects = listOf(
    ":printscript-common",
    ":printscript-api",
    ":printscript-lexer",
    ":printscript-parser",
    ":printscript-interpreter"
)

configure(publishedProjects.map(::project)) {
    group = "com.github.ingsis26g14"
    version = "1.0"

    apply(plugin = "maven-publish")

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        extensions.configure<PublishingExtension> {

            publications {
                create<MavenPublication>("printScript") {
                    from(components["java"])
                    artifactId = project.name
                }
            }

            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri(
                        "https://maven.pkg.github.com/IngSis26-G14/printScript"
                    )

                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }
}

tasks.register("publish") {
    group = "publishing"
    description = "Publishes all public PrintScript artifacts."

    dependsOn(
        publishedProjects.map { "$it:publish" }
    )
}
