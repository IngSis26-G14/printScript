plugins {
    id("shared-build-config")
}

version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":printscript-cli"))
    implementation(project(":printscript-api"))
    implementation(project(":printscript-common"))
    implementation(project(":printscript-lexer"))
    implementation(project(":printscript-parser"))
    implementation(project(":printscript-validator"))
    implementation(project(":printscript-linter"))
    implementation(project(":printscript-formatter"))
}

tasks.register<Sync>("pluginLibraries") {
    dependsOn(tasks.jar)
    from(tasks.jar)
    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("plugin-libraries"))
}
