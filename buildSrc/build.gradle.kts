repositories {
    mavenCentral()
    google()
    jcenter()
}

plugins {
    `kotlin-dsl`
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

dependencies {
    val kotlinVersion by extra {
        "1.3.70"
    }
    val dokka_version by extra {
        "0.10.1"
    }
    val dokkaPlugin by extra {
        "org.jetbrains.dokka:dokka-gradle-plugin:${dokka_version}"
    }

    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(dokkaPlugin)
    implementation(gradleApi())
    implementation(localGroovy())
}