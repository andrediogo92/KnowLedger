plugins {
    kotlin("jvm")
    id(Plugins.base)
}

pluginConfiguration {
    packageName = "org.knowledger.encoding"
    module = "encoding-extensions"
    requiresOptIn = true
    experimentalContracts = true
}

dependencies {
    implementation(project(":ledger:core:data"))
    implementation(Libs.commonsCodec)
}

version = "0.3"
