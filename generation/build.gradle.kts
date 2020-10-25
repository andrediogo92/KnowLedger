version = "0.0"

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id(Plugins.base)
}

pluginConfiguration {
    packageName = "org.knowledger.generation"
    module = "generation"
}


dependencies {
    implementation(project(":annotations"))
    implementation(project(":ledger:core"))

    implementation(Libs.kotlinPoet)
    implementation(Libs.autoService)
    kapt(Libs.autoService)
}
