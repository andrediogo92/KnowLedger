version = "0.3"

plugins {
    kotlin("jvm")
    id(Plugins.serial)
    id(Plugins.jmh)
}

serialPlugin {
    packageName = "org.knowledger.ledger"
    module = "ledger"
    experimentalContracts = true
    inlineClasses = true
}

jmh {
    jmhVersion = "1.24"
    resultFormat = "CSV"
    duplicateClassesStrategy = DuplicatesStrategy.EXCLUDE
    isIncludeTests = true
}

dependencies {
    //Project dependencies
    implementation(project(":base64-extensions"))
    implementation(project(":collections-extensions"))
    implementation(project(":results"))
    api(project(":ledger:storage"))
    implementation(project(":ledger:orient"))

    jmhImplementation(Libs.jmh)
    testImplementation(project(":testing"))
}
