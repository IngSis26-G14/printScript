plugins {
    id("shared-build-config")
    application
}

dependencies {
    implementation(project(":printscript-api"))
    implementation(project(":printscript-common"))
    implementation(project(":printscript-lexer"))
    implementation(project(":printscript-parser"))
    implementation(project(":printscript-validator"))
    implementation(project(":printscript-linter"))

    // Agregar cuando existan:
    // implementation(project(":printscript-interpreter"))
    // implementation(project(":printscript-formatter"))

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("cli.MainKt")
}
