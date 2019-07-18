buildscript {
    repositories {
        jcenter()
        maven("https://kotlin.bintray.com/kotlinx")
    }

    dependencies {
        classpath(kotlin("gradle-plugin", Versions.kotlin))
        classpath(kotlin("noarg", Versions.kotlin))
    }

}

plugins {
    base
    kotlin("jvm") version
            Versions.kotlin apply
            false
    id("org.jetbrains.kotlin.plugin.noarg") version
            Versions.kotlin apply
            false
    id("kotlinx-serialization") version
            Versions.kotlin apply
            false
}

allprojects {
    group = "org.knowledger"

    repositories {
        mavenCentral()
        jcenter()
        maven("https://kotlin.bintray.com/kotlinx")
    }
}
