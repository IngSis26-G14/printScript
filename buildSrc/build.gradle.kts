plugins {
    `kotlin-dsl`
    `maven-publish`
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.7")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.22.0")
    implementation("org.jetbrains.kotlinx:kover:0.6.1")
}