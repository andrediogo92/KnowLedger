plugins {
    kotlin("jvm")
    id(Plugins.base)
}

version = "0.1"

pluginConfiguration {
    packageName = "org.knowledger.agent.ontologies"
    module = "agent/ontologies"
}

dependencies {
    Libs.jade.forEach(::implementation)
    implementation(project(":encoding-extensions"))
    implementation(project(":collections-extensions"))
}
