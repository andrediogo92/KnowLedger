repositories {
    mavenCentral()
    google()
    jcenter()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    val kotlinVersion by extra {
        "1.3.50"
    }
    val dokka by extra {
        "0.9.18"
    }
    val dokkaPlugin by extra {
        "org.jetbrains.dokka:dokka-gradle-plugin:${dokka}"
    }

    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(dokkaPlugin)
    implementation(gradleApi())
    implementation(localGroovy())
}