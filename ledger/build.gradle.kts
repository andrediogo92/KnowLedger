import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "0.1"

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.noarg")
//    id("kotlinx-serialization")
}

dependencies {
    //Project dependencies
    compileOnly(project(":annotations"))
    implementation(project(":ledger-core"))

    //Annotation Processing
    kapt(Libs.moshiCodeGen)
    kapt(project(":generation"))


    //Regular dependencies
    implementation(kotlin("stdlib", Versions.kotlin))
    implementation(kotlin("reflect", Versions.kotlin))
    //implementation(Libs.arrowK)
    implementation(Libs.bouncyCastle)
    //implementation(Libs.coroutines)
    implementation(Libs.jol)
    Libs.moshi.forEach {
        implementation(it)
    }
    //implementation(Libs.serialization)
    Libs.tinylog.forEach {
        implementation(it)
    }


    //Test dependencies
    testImplementation(Libs.assertK)
    testImplementation(Libs.commonsRNG)
    testImplementation(Libs.jUnitApi)
    testImplementation(project(":ledger-core")) {
        capabilities {
            // Indicate we want a variant with a specific capability
            requireCapability("org.knowledger.ledger.core:test")
        }
    }
    Libs.jUnitRuntime.forEach {
        testRuntimeOnly(it)
    }
}

tasks {
    withType<JavaExec> {
        jvmArgs("-Djdk.attach.allowAttachSelf=true")
    }

    withType<Test> {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs += "-XXLanguage:+InlineClasses"
        kotlinOptions.jvmTarget = "1.8"
    }
}


sourceSets["main"].withConvention(KotlinSourceSet::class) {
    kotlin.srcDir("${buildDir.absolutePath}/generated/source/kaptKotlin/")
}