plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

serialPlugin {
    packageName = "org.knowledger.ledger.storage"
    module = "ledger/storage"
    requiresOptIn = true
    experimentalContracts = true
}

dependencies {
    //Project dependencies
    implementation(project(":base64-extensions"))
    implementation(project(":collections-extensions"))
    implementation(project(":results"))
    api(project(":ledger:core"))

    testImplementation(project(":testing"))
}
