import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.noarg")
}


repositories {
    mavenCentral()
    jcenter()
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

tasks {
    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs += "-XXLanguage:+InlineClasses"
        kotlinOptions.jvmTarget = "1.8"
    }
}
