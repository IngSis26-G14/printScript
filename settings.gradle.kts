plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "printScript"

include(
    "printscript-common",
    "printscript-lexer",
    "printscript-parser",
    "printscript-api",
    "printscript-interpreter",
    "printscript-validator"
)
