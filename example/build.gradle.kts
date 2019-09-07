import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.noarg")
    id("org.jetbrains.dokka")
    id("kotlinx-serialization")
    application
}

repositories {
    maven("https://kotlin.bintray.com/ktor")
    maven("https://jade.tilab.com/maven")
}

dependencies {
    implementation(kotlin("stdlib", Versions.kotlin))

    //Project dependencies
    implementation(project(":ledger"))
    implementation(project(":ledger-core"))
    implementation(project(":agent"))

    Libs.jade.forEach {
        implementation(it)
    }

    //Standard dependencies
    Libs.tinylog.forEach {
        implementation(it)
    }

    //Test dependencies
    testImplementation(Libs.assertK)
    testImplementation(Libs.commonsRNG)
    testImplementation(Libs.jUnitApi)
    Libs.jUnitRuntime.forEach {
        testRuntimeOnly(it)
    }
}

tasks {
    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }


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