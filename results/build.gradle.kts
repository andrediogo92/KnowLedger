plugins {
    kotlin("jvm")
    id(Plugins.base)
}

pluginConfiguration {
    packageName = "org.knowledger.ledger.storage.results"
    module = "results"
    experimentalContracts = true
}

dependencies {
    api(Libs.kotlinResult)
}

version = "0.2"