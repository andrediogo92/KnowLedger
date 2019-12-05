plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

serialPlugin {
    inlineClasses = true
    packageName = "org.knowledger.ledger.database"
    module = "ledger-core/db"
}

dependencies {
    implementation(project(":ledger-core:data"))
    implementation(project(":ledger-core:results"))
    implementation(project(":base64-extensions"))
}

version = "0.1"
