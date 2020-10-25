version = "0.3"

plugins {
    kotlin("jvm")
    id(Plugins.serial)
    id(Plugins.jmh)
}

pluginConfiguration {
    packageName = "org.knowledger.ledger"
    module = "ledger"
    experimentalContracts = true
    inlineClasses = true
    requiresOptIn = true
    experimentalOptIn = true
}

jmh {
    jmhVersion = "1.25.1"
    resultFormat = "CSV"
    duplicateClassesStrategy = DuplicatesStrategy.EXCLUDE
    isIncludeTests = true
}

dependencies {
    //Project dependencies
    implementation(project(":encoding-extensions"))
    implementation(project(":collections-extensions"))
    implementation(project(":results"))
    api(project(":ledger:storage"))
    implementation(project(":ledger:orient"))

    implementation(kotlin("reflect"))

    jmhImplementation(Libs.jmh)
    testImplementation(project(":testing"))
}

subprojects {
    version = this.version
}
