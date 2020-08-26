buildscript {
    repositories {
        jcenter {
            content {
                excludeGroup("org.jetbrains.kotlinx")
            }
        }
        maven("https://kotlin.bintray.com/kotlinx") {
            metadataSources {
                gradleMetadata()
            }
            content {
                includeGroup("org.jetbrains.kotlin")
                includeGroup("org.jetbrains.kotlinx")
            }
        }
        maven("https://plugins.gradle.org/m2/") {
            metadataSources {
                gradleMetadata()
            }
            content {
                excludeGroup("org.jetbrains.kotlinx")
            }
        }
    }

    dependencies {
        classpath(kotlin("noarg", Versions.kotlin))
        classpath(kotlin("serialization", Versions.kotlin))
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

    //Configure projects with exclusive filters.
    repositories {
        jcenter {
            content {
                excludeGroup("com.tilab.jade")
            }
        }
        maven("https://kotlin.bintray.com/kotlinx") {
            metadataSources {
                gradleMetadata()
            }

            content {
                includeGroup("org.jetbrains.kotlin")
                includeGroup("org.jetbrains.kotlinx")
                excludeGroup("com.tilab.jade")
            }
        }

        //Jade dependencies will only be searched in this repository.
        //Jade direct dependency is broken due to TLS certification issues.
        //maven("https://jade.tilab.com/maven") {
        mavenLocal {
            content {
                includeGroup("com.tilab.jade")
            }
        }
    }


}

