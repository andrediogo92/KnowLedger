plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

pluginConfiguration {
    packageName = "org.knowledger.ledger.testing"
    module = "testing"
    requiresOptIn = true
    experimentalContracts = true
}

dependencies {
    implementation(project(":encoding-extensions"))
    implementation(project(":collections-extensions"))
    implementation(project(":results"))
    api(project(":ledger:core"))
    implementation(project(":ledger:storage"))
    implementation(Libs.commonsRNG)
}

version = "0.3"