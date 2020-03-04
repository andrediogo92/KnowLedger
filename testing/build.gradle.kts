plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

serialPlugin {
    packageName = "org.knowledger.ledger.testing"
    module = "testing"
    requiresOptIn = true
}

version = "0.2"

dependencies {
    implementation(project(":ledger-core"))
    implementation(Libs.commonsRNG)
}
