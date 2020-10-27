plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

pluginConfiguration {
    packageName = "org.knowledger.ledger.core"
    module = "ledger/core"
    experimentalContracts = true
    requiresOptIn = true
    experimentalOptIn = true
    inlineClasses = true
}

dependencies {
    implementation(project(":encoding-extensions"))
    implementation(project(":collections-extensions"))
    implementation(project(":results"))
    api(project(":ledger:core:crypto"))
    api(project(":ledger:core:data"))
    api(project(":ledger:core:db"))
    api(project(":ledger:core:data-serial"))
    api(Libs.datetime)
    api(Libs.uuid)

    testImplementation(project(":testing"))
}