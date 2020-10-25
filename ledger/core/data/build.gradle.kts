plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

pluginConfiguration {
    packageName = "org.knowledger.ledger.core.data"
    module = "ledger/core/data"
    requiresOptIn = true
    experimentalOptIn = true
}