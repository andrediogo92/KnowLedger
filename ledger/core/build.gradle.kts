version = "0.3"


plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

serialPlugin {
    packageName = "org.knowledger.ledger.core"
    module = "ledger/core"
    requiresOptIn = true
}

dependencies {
    implementation(project(":base64-extensions"))
    implementation(project(":collections-extensions"))
    implementation(project(":results"))
    api(project(":ledger:core:crypto"))
    api(project(":ledger:core:data"))
    api(project(":ledger:core:db"))
    api(project(":ledger:core:kserial"))

    testImplementation(project(":testing"))
}

subprojects {
    version = this.version
}