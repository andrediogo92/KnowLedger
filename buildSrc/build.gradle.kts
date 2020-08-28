repositories {
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
        "1.4.0"
    }
//    val dokkaVersion by extra {
//       "1.4.0-rc"
//    }
    implementation(kotlin("gradle-plugin", kotlinVersion))
//    implementation("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
    implementation(gradleApi())
    implementation(localGroovy())
}