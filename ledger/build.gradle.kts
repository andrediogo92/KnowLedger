import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "0.1"

plugins {
    kotlin("jvm")
    id(Plugins.serial)
    id(Plugins.jmh)
}

serialPlugin {
    packageName = "org.knowledger.ledger"
    module = "ledger"
}

jmh {
    duplicateClassesStrategy = DuplicatesStrategy.EXCLUDE
    isIncludeTests = true
}

dependencies {
    //Project dependencies
    api(project(":ledger-core"))
    implementation(project(":ledger-orient"))

    jmhImplementation(Libs.jmh)

    testImplementation(project(":testing"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.Experimental"
}
