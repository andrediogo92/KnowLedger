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
    implementation(project(":base64-extensions"))
    implementation(project(":collections-extensions"))
    implementation(project(":results"))
    implementation(project(":ledger:storage"))
    implementation(Libs.commonsRNG)
}

version = "0.3"