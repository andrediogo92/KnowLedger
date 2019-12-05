version = "0.1"


plugins {
    kotlin("jvm")
    id(Plugins.serial)
}

serialPlugin {
    packageName = "org.knowledger.ledger.core"
    module = "ledger-core"
}

dependencies {
    api(project(":base64-extensions"))
    api(project(":collections-extensions"))
    api(project(":ledger-core:crypto"))
    api(project(":ledger-core:data"))
    api(project(":ledger-core:db"))
    api(project(":ledger-core:kserial"))
    api(project(":ledger-core:results"))

    testImplementation(project(":testing"))
}