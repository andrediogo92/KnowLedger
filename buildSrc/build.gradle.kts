repositories {
    mavenCentral()
    google()
    jcenter()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    val dokka = "0.9.18"
    val dokkaPlugin =
        "org.jetbrains.dokka:dokka-gradle-plugin:${dokka}"

    implementation(kotlin("gradle-plugin", "1.3.50"))
    implementation(dokkaPlugin)
    implementation(gradleApi())
    implementation(localGroovy())
}