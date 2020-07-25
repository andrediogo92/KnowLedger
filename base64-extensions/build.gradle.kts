version = "0.2"


plugins {
    kotlin("jvm")
    id(Plugins.base)
}

basePlugin {
    module = "base64-extensions"
    requiresOptIn = true
}

dependencies {
    implementation(project(":ledger:core:data"))
}