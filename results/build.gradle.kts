plugins {
    kotlin("jvm")
    id(Plugins.base)
}

basePlugin {
    module = "results"
    packageName = "org.knowledger.ledger.results"
}

dependencies {
    api(Libs.kotlinResult)
}

version = "0.2"