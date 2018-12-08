import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.noarg")
    id("kotlinx-serialization")
}

repositories {
    maven("https://kotlin.bintray.com/ktor")
    maven("http://jade.tilab.com/maven")
}

dependencies {
    implementation(kotlin("stdlib", Versions.kotlin))
    implementation(Libs.coroutines)
    implementation(Libs.serialization)
    Libs.ktor.forEach {
        implementation(it)
    }
    implementation(Libs.eclipsePaho)
    Libs.jade.forEach {
        implementation(it)
    }
    implementation(Libs.klog)
    implementation(project(":blockchain"))
    Libs.slf4j.forEach {
        runtimeOnly(it)
    }
    testImplementation(Libs.jUnitApi)
    Libs.jUnitRuntime.forEach {
        testRuntimeOnly(it)
    }
}


configure<ApplicationPluginConvention> {
    mainClassName = "pt.um.lei.masb.agent.Container"
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}