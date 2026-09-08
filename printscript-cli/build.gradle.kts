plugins {
    id("shared-build-config")
    id("org.jetbrains.kotlinx.kover")
    application
}

dependencies {
    implementation(project(":printscript-api"))
    implementation(project(":printscript-common"))
    implementation(project(":printscript-lexer"))
    implementation(project(":printscript-parser"))
    implementation(project(":printscript-validator"))
    implementation(project(":printscript-linter"))
    implementation(project(":printscript-interpreter"))

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("cli.MainKt")
}
