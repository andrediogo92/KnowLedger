
version = "0.1"

plugins {
    kotlin("jvm")
    id(Plugins.serial)
    id(Plugins.jmh)
}

serialPlugin {
    packageName = "org.knowledger.ledger"
    module = "ledger"
}

jmh {
    isIncludeTests = true
}

dependencies {
    //Project dependencies
    api(project(":ledger-core"))
    implementation(project(":ledger-orient"))
    implementation(Libs.jmh)

    testImplementation(Libs.commonsRNG)
    testImplementation(project(":testing"))
}
