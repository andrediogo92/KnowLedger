plugins {
    kotlin("jvm")
    id("org.knowledger.plugin.base")
    application
}

repositories {
    maven("https://jade.tilab.com/maven")
}

baseJVM {
    packageName = "org.knowledger.example"
    module = "example"
}

dependencies {
    //Project dependencies
    implementation(project(":ledger"))
    implementation(project(":ledger-core"))
    implementation(project(":agent"))

    Libs.jade.forEach(::implementation)

    //Test dependencies
    testImplementation(Libs.commonsRNG)
}