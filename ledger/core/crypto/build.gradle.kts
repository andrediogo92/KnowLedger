plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

pluginConfiguration {
    packageName = "org.knowledger.ledger.crypto"
    module = "ledger/core/crypto"
    requiresOptIn = true
    experimentalOptIn = true
    experimentalContracts = true
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(project(":collections-extensions"))
    implementation(project(":encoding-extensions"))
    implementation(project(":ledger:core:data"))
    implementation(project(":ledger:core:data-serial"))

    implementation(Libs.bouncyCastle)

    testImplementation(project(":testing"))
}