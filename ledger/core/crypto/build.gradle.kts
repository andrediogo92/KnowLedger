plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

serialPlugin {
    packageName = "org.knowledger.ledger.crypto"
    module = "ledger/core/crypto"
    requiresOptIn = true
    experimentalOptIn = true
    experimentalContracts = true
}

dependencies {
    implementation(project(":base64-extensions"))
    implementation(project(":collections-extensions"))
    implementation(project(":ledger:core:kserial"))
    implementation(project(":ledger:core:data"))

    implementation(Libs.bouncyCastle)

    testImplementation(project(":testing"))
}