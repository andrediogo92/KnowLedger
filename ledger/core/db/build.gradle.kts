plugins {
    kotlin("jvm")
    id(Plugins.base)
}

pluginConfiguration {
    packageName = "org.knowledger.ledger.database"
    module = "ledger/core/db"
}

dependencies {
    implementation(project(":ledger:core:data"))
    implementation(project(":encoding-extensions"))
    implementation(project(":results"))
}
