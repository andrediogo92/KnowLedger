import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.noarg")
}


repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    Libs.orientDB.forEach {
        implementation(it)
    }
    implementation(Libs.bouncyCastle)
    implementation(Libs.klog)
    implementation(Libs.jol)
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + "-XXLanguage:+InlineClasses"
    jvmTarget = "1.8"
}
