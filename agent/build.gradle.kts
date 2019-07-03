import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.noarg")
//    id("kotlinx-serialization")
}

repositories {
    maven("https://kotlin.bintray.com/ktor")
    maven("https://jade.tilab.com/maven")
}

dependencies {
    //Project dependencies
    implementation(project(":common"))
    implementation(project(":ledger"))


    //Annotation Processing
    kapt(Libs.moshiCodeGen)


    //Regular dependencies
    implementation(kotlin("stdlib", Versions.kotlin))
    //implementation(Libs.arrowK)
    implementation(Libs.coroutines)
    implementation(Libs.eclipsePaho)
    Libs.jade.forEach {
        implementation(it)
    }
    Libs.ktor.forEach {
        implementation(it)
    }
    Libs.moshi.forEach {
        implementation(it)
    }
    //implementation(Libs.serialization)
    Libs.tinylog.forEach {
        implementation(it)
    }

    //Test dependencies
    testImplementation(Libs.assertK)
    testImplementation(Libs.jUnitApi)
    Libs.jUnitRuntime.forEach {
        testRuntimeOnly(it)
    }
}


configure<ApplicationPluginConvention> {
    mainClassName = "org.knowledger.agent.Container"
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
