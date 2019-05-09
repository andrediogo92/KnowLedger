plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.noarg")
}


repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":annotations"))
    implementation(kotlin("stdlib-jdk8"))
    //Code generation library for kotlin, highly recommended
    implementation(Libs.kotlinPoet)
    implementation(Libs.autoService)
    kapt(Libs.autoService)
}

