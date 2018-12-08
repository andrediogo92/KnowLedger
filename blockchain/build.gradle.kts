import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.noarg")
    id("kotlinx-serialization")
}

dependencies {
    implementation(kotlin("reflect", Versions.kotlin))
    implementation(kotlin("stdlib", Versions.kotlin))
    implementation(Libs.serialization)
    Libs.orientDB.forEach {
        implementation(it)
    }
    implementation(Libs.klog)
    implementation(Libs.bouncyCastle)
    implementation(Libs.jol)
    Libs.slf4j.forEach {
        implementation(it)
    }
    testImplementation(Libs.bouncyCastle)
    testImplementation(project(":blockchain"))
    testImplementation(Libs.jUnitApi)
    Libs.jUnitRuntime.forEach {
        testRuntimeOnly(it)
    }
}


tasks.withType<JavaExec> {
    jvmArgs("-Djdk.attach.allowAttachSelf=true")
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