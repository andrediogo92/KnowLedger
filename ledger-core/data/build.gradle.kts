plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

serialPlugin {
    inlineClasses = true
    packageName = "org.knowledger.ledger.core.base"
    module = "ledger-core/data"
    requiresOptIn = true
}
