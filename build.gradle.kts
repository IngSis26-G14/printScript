import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("base")
    id("org.jetbrains.kotlinx.kover")
    id("co.uzzu.dotenv.gradle") version "4.0.0"
}

repositories {
    mavenCentral()
}

koverMerged {
    enable()
    htmlReport { onCheck = true }
    verify { rule { bound { minValue = 0 } } }

    filters {
        projects {
            excludes += listOf(":printscript-common")
        }
    }
}

tasks.named("check") {
    dependsOn("koverMergedVerify")
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