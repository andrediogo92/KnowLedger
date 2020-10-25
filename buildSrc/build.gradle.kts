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
    val kotlinVersion: String by extra {
        "1.4.10"
    }
    compileOnly("org.jetbrains.dokka:dokka-core:$kotlinVersion")
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:$kotlinVersion")
    // Will apply the plugin to all dokka tasks
    implementation(gradleApi())
    implementation(localGroovy())
}