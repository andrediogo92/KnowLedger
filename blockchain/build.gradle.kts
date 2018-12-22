import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.noarg")
    id("kotlinx-serialization")
}

dependencies {
    compile(kotlin("reflect", Versions.kotlin))
    compile(kotlin("stdlib", Versions.kotlin))
    implementation(Libs.coroutines)
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
    testCompile(kotlin("test"))
    testCompile(kotlin("test-junit"))
    testCompile(Libs.assertK)
    testImplementation(Libs.bouncyCastle)
    testImplementation(Libs.jUnitApi)
    testImplementation(project(":blockchain"))
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
    freeCompilerArgs += "-XXLanguage:+InlineClasses"
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}