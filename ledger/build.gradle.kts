import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.noarg")
//    id("kotlinx-serialization")
}

dependencies {
    //Project dependencies
    compileOnly(project(":annotations"))
    implementation(project(":common"))

    //Annotation Processing
    kapt(Libs.moshiCodeGen)
    kapt(project(":generation"))


    //Regular dependencies
    implementation(kotlin("stdlib", Versions.kotlin))
    implementation(kotlin("reflect", Versions.kotlin))
    implementation(kotlin("test", Versions.kotlin))
    implementation(kotlin("test-junit", Versions.kotlin))
    //implementation(Libs.arrowK)
    implementation(Libs.bouncyCastle)
    //implementation(Libs.coroutines)
    implementation(Libs.klog)
    implementation(Libs.jol)
    Libs.moshi.forEach {
        implementation(it)
    }
//    Libs.orientDB.forEach {
//        implementation(it)
//    }
    //implementation(Libs.serialization)
    Libs.slf4j.forEach {
        implementation(it)
    }


    //Test dependencies
    testImplementation(Libs.assertK)
    testImplementation(Libs.commonsRNG)
    testImplementation(Libs.jUnitApi)
    testImplementation(project(":common")) {
        capabilities {
            // Indicate we want a variant with a specific capability
            requireCapability("pt.um.masb.common:test")
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