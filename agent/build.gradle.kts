plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

version = "0.1"

serialPlugin {
    packageName = "org.knowledger.agent"
    module = "agent"
}

dependencies {
    //Project dependencies
    implementation(project(":ledger"))
    implementation(project(":agent-core"))


    //Regular dependencies
    Libs.jade.forEach(::implementation)
}


