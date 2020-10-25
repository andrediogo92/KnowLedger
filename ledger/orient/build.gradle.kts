plugins {
    kotlin("jvm")
    id(Plugins.base)
}

pluginConfiguration {
    packageName = "org.knowledger.database.orient"
    module = "ledger/orient"
    inlineClasses = true
}

dependencies {
    implementation(project(":collections-extensions"))
    implementation(project(":ledger:core:db"))
    implementation(project(":ledger:core:data"))

    Libs.orientDB.forEach(::implementation)
}