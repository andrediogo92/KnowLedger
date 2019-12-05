buildscript {
    repositories {
        jcenter()
        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath(kotlin("gradle-plugin", Versions.kotlin))
        classpath(kotlin("noarg", Versions.kotlin))
        classpath(kotlin("serialization", Versions.kotlin))
        classpath(Libs.dokkaPlugin)
        classpath(Libs.jmhPlugin)
    }

}

plugins {
    id("org.jetbrains.kotlin.plugin.noarg") version
            Versions.kotlin apply
            false
    id("org.jetbrains.kotlin.plugin.serialization") version
            Versions.kotlin apply
            false
}

allprojects {
    group = "org.knowledger"

    repositories {
        mavenCentral()
        jcenter()
        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://jade.tilab.com/maven")
    }

}
