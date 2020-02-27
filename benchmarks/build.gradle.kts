plugins {
    kotlin("jvm")
    id(Plugins.base)
    id(Plugins.jmh)
}

version = "0.1"

basePlugin {
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
    implementation(Libs.jmh)

    //Test dependencies
    testImplementation(Libs.commonsRNG)

}