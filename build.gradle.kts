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

tasks.named("check") { dependsOn("koverMergedVerify") }
