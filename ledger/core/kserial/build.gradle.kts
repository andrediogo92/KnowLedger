plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

serialPlugin {
    packageName = "org.knowledger.ledger.core.serial"
    module = "ledger/core/kserial"
    experimentalContracts = true
}

dependencies {
    implementation(project(":ledger:core:data"))
    implementation(project(":base64-extensions"))
    implementation(Libs.datetime)
}