plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

version = "0.1"

serialPlugin {
    packageName = "org.knowledger.ledger.crypto"
    module = "ledger-core/crypto"
}

dependencies {
    implementation(project(":base64-extensions"))
    implementation(project(":collections-extensions"))
    implementation(project(":ledger-core:kserial"))
    implementation(project(":ledger-core:data"))

    implementation(Libs.bouncyCastle)

    testImplementation(project(":testing"))
    testImplementation(project(":ledger-core"))
    testImplementation(Libs.commonsRNG)
}