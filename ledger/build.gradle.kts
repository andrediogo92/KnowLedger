version = "0.1"

plugins {
    kotlin("jvm")
    id("org.knowledger.plugin.base")
    id(Plugins.jmh)
}

baseJVM {
    packageName = "org.knowledger.ledger"
    module = "ledger"
}

jmh {
    isIncludeTests = true
}

dependencies {
    //Project dependencies
    implementation(project(":ledger-core"))
    implementation(project(":ledger-orient"))
    implementation(project(":ledger-crypto"))
    implementation(Libs.jmh)

    testImplementation(Libs.commonsRNG)
    testImplementation(project(":ledger-core")) {
        capabilities {
            // Indicate we want a variant with a specific capability
            requireCapability("org.knowledger.ledger.core:test")
        }
    }
}
