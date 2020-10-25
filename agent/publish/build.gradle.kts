plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

group = "org.knowledger"
version = "0.1"

pluginConfiguration {
    packageName = "org.knowledger.agent.publish"
    module = "agent/publish"
}


dependencies {
    implementation(project(":agent"))
    implementation(project(":ledger"))
    implementation(Libs.eclipsePaho)
    Libs.jade.forEach(::implementation)
}
