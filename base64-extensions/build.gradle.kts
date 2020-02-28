import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "0.1"


plugins {
    kotlin("jvm")
    id(Plugins.base)
}

basePlugin {
    module = "base64-extensions"
}

dependencies {
    implementation(project(":ledger-core:data"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.Experimental"
}