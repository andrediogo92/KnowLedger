import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.noarg")
//    id("kotlinx-serialization")
}

dependencies {
    //Annotation Processing
    kapt(Libs.moshiCodeGen)


    //Regular dependencies
    implementation(kotlin("stdlib", Versions.kotlin))
    implementation(kotlin("reflect", Versions.kotlin))
    //implementation(Libs.arrowK)
    implementation(Libs.bouncyCastle)
    implementation(Libs.coroutines)
    implementation(Libs.klog)
    implementation(Libs.jol)
    Libs.moshi.forEach {
        implementation(it)
    }
    Libs.orientDB.forEach {
        implementation(it)
    }
    //implementation(Libs.serialization)
    Libs.slf4j.forEach {
        implementation(it)
    }


    //Test dependencies
    testImplementation(project(":ledger"))
    testImplementation(Libs.assertK)
    testImplementation(Libs.commonsRNG)
    testImplementation(Libs.bouncyCastle)
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
    freeCompilerArgs = freeCompilerArgs + "-XXLanguage:+InlineClasses"
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}