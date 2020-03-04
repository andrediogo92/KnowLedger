version = "0.2"

plugins {
    kotlin("jvm")
    id(Plugins.serial)
    id(Plugins.jmh)
}

serialPlugin {
    packageName = "org.knowledger.ledger"
    module = "ledger"
    requiresOptIn = true
}

jmh {
    duplicateClassesStrategy = DuplicatesStrategy.EXCLUDE
    isIncludeTests = true
}

dependencies {
    //Project dependencies
    api(project(":ledger-core"))
    implementation(project(":ledger-orient"))

    jmhImplementation(Libs.jmh)
    testImplementation(project(":testing"))
}
