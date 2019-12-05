plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

version = "0.1"

serialPlugin {
    packageName = "org.knowledger.ledger.core.serial"
    module = "ledger-core/kserial"
}

dependencies {
    implementation(project(":ledger-core:data"))
}