plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

group = "org.knowledger"
version = "0.1"

serialPlugin {
    packageName = "org.knowledger.ledger.agent-publish"
    module = "agent-publish"
}


dependencies {
    implementation(project(":agent"))
    implementation(project(":agent-core"))
    implementation(project(":ledger"))
    implementation(project(":ledger-core"))
    implementation(Libs.eclipsePaho)
    Libs.jade.forEach(::implementation)
}
