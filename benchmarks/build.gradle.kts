plugins {
    kotlin("jvm")
    id("org.knowledger.plugin.base")
    id(Plugins.jmh)
}

version = "0.1"

repositories {
    maven("https://jade.tilab.com/maven")
}

baseJVM {
    packageName = "org.knowledger.benchmarks"
    module = "benchmarks"
}

jmh {
    isIncludeTests = true
}

dependencies {
    //Project dependencies
    implementation(project(":ledger"))
    implementation(project(":ledger-core"))
    implementation(project(":agent"))

    Libs.jade.forEach(::implementation)

    //Test dependencies
    testImplementation(Libs.commonsRNG)

    Libs.jmh.forEach(::jmh)
}