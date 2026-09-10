plugins {
    id("io.gitlab.arturbosch.detekt")
    id("com.diffplug.spotless")
    id("org.jetbrains.kotlinx.kover")
    kotlin("jvm")
}

repositories { mavenCentral() }

dependencies { testImplementation(kotlin("test")) }

tasks.test { useJUnitPlatform() }

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/detekt/detekt.yml"))
}

kotlin { jvmToolchain(17) }

tasks.check { dependsOn("detekt", "spotlessCheck", "koverVerify") }

tasks.named("build") {
    dependsOn("spotlessCheck", "check")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("build/**/*.kt")
        ktlint().editorConfigOverride(
            mapOf(
                "indent_size" to "4",
                "insert_final_newline" to "true"
            )
        )
        trimTrailingWhitespace()
    }
}

pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    apply(plugin = "maven-publish")

    val envFile = rootProject.file(".env")
    val envVars: Map<String, String> = if (envFile.exists()) {
        envFile.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .mapNotNull { line ->
                val idx = line.indexOf('=')
                if (idx <= 0) null
                else line.substring(0, idx).trim() to line.substring(idx + 1).trim()
            }
            .toMap()
    } else {
        emptyMap()
    }

    fun credential(key: String): String? =
        envVars[key] ?: System.getenv(key)

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
                url = uri("https://maven.pkg.github.com/IngSis26-G14/printScript")

                credentials {
                    username = credential("GITHUB_ACTOR")
                    password = credential("GITHUB_TOKEN")
                }
            }
        }
    }
}
