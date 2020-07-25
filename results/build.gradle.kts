plugins {
    kotlin("jvm")
    id(Plugins.base)
}

basePlugin {
    module = "results"
    packageName = "org.knowledger.ledger.storage.results"
    experimentalContracts = true
}

dependencies {
    api(Libs.kotlinResult)
}

version = "0.2"