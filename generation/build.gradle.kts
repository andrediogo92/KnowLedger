version = "0.0"

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("org.knowledger.plugin.docs")
}

docs {
    module = "generation"
}


dependencies {
    implementation(project(":annotations"))
    implementation(project(":ledger-core"))

    implementation(Libs.kotlinPoet)
    implementation(Libs.autoService)
    kapt(Libs.autoService)
}
