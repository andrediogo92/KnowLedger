version = "0.2"


plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

serialPlugin {
    packageName = "org.knowledger.ledger.core"
    module = "ledger-core"
    requiresOptIn = true
}

dependencies {
    api(project(":base64-extensions"))
    api(project(":collections-extensions"))
    api(project(":ledger-core:crypto"))
    api(project(":ledger-core:data"))
    api(project(":ledger-core:db"))
    api(project(":ledger-core:kserial"))
    api(project(":results"))

    testImplementation(project(":testing"))
}

subprojects {
    version = this.version
}