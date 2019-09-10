plugins {
    kotlin("jvm")
    id("org.knowledger.plugin.base")
}

version = "0.1"

baseJVM {
    packageName = "org.knowledger.ledger.crypto"
    module = "ledger-crypto"
}

dependencies {
    implementation(project(":ledger-core"))

    implementation(Libs.bouncyCastle)

    testImplementation(project(":ledger-core")) {
        capabilities {
            // Indicate we want a variant with a specific capability
            requireCapability("org.knowledger.ledger.core:test")
        }
    }
    testImplementation(Libs.commonsRNG)
}