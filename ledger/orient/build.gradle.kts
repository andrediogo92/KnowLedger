plugins {
    kotlin("jvm")
    id(Plugins.docs)
}

docsPlugin {
    inlineClasses = true
    module = "ledger/orient"
}

dependencies {
    implementation(project(":collections-extensions"))
    implementation(project(":ledger:core:db"))
    implementation(project(":ledger:core:data"))

    Libs.orientDB.forEach(::implementation)
}