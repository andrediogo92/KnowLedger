version = "0.0"

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id(Plugins.docs)
}

docsPlugin {
    module = "generation"
}


dependencies {
    implementation(project(":annotations"))
    implementation(project(":ledger-core"))

    implementation(Libs.kotlinPoet)
    implementation(Libs.autoService)
    kapt(Libs.autoService)
}
