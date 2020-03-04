plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

serialPlugin {
    packageName = "org.knowledger.ledger.testing"
    module = "testing"
    requiresOptIn = true
}

dependencies {
    implementation(project(":ledger-core"))
    implementation(Libs.commonsRNG)
}

version = "0.2"