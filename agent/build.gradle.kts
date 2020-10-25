plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

version = "0.1"

pluginConfiguration {
    packageName = "org.knowledger.agent"
    module = "agent"
}

dependencies {
    //Project dependencies
    implementation(project(":ledger"))
    api(project(":agent:ontologies"))


    //Regular dependencies
    Libs.jade.forEach(::implementation)
}


