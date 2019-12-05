plugins {
    kotlin("jvm")
    id(Plugins.base)
}

version = "0.1"

basePlugin {
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


