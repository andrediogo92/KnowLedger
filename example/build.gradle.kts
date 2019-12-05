plugins {
    kotlin("jvm")
    id(Plugins.base)
    application
}

basePlugin {
    packageName = "org.knowledger.example"
    module = "example"
}

dependencies {
    //Project dependencies
    implementation(project(":ledger"))
    implementation(project(":ledger-core"))
    implementation(project(":agent"))
    implementation(project(":agent-core"))

    Libs.jade.forEach(::implementation)

    //Test dependencies
    testImplementation(Libs.commonsRNG)
}