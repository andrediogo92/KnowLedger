plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

pluginConfiguration {
    packageName = "org.knowledger.ledger.core.serial"
    module = "ledger/core/data-serial"
    experimentalContracts = true
}

dependencies {
    implementation(project(":ledger:core:data"))
    implementation(project(":encoding-extensions"))
    implementation(Libs.datetime)
}