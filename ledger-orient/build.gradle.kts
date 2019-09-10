plugins {
    kotlin("jvm")
    id("org.knowledger.plugin.docs")
}

version = "0.1"

docs {
    inlineClasses = true
    module = "ledger-orient"
}

dependencies {
    implementation(project(":ledger-core"))
    Libs.orientDB.forEach {
        implementation(it)
    }
}