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
    //JMH dependencies
    jmhImplementation(project(":ledger"))
    jmhImplementation(project(":agent"))
    jmhImplementation(Libs.jmh)

}