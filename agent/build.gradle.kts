
version = "0.1"


plugins {
    kotlin("jvm")
    id("org.knowledger.plugin.base")
}

repositories {
    maven("https://jade.tilab.com/maven")
}

baseJVM {
    packageName = "org.knowledger.agent"
    module = "agent"
}

dependencies {
    //Project dependencies
    implementation(project(":ledger"))
    implementation(project(":ledger-core"))
    implementation(project(":ledger-crypto"))
    implementation(project(":agent-core"))


    //Regular dependencies
    implementation(Libs.eclipsePaho)
    Libs.jade.forEach {
        implementation(it)
    }
}


