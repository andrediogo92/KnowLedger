import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "0.1"


plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.noarg")
    id("org.jetbrains.dokka")
    id("kotlinx-serialization")
}

repositories {
    maven("https://jade.tilab.com/maven")
}

dependencies {
    //Project dependencies
    implementation(project(":ledger"))
    implementation(project(":ledger-core"))
    implementation(project(":agent-core"))


    //Regular dependencies
    implementation(kotlin("stdlib", Versions.kotlin))
    implementation(Libs.eclipsePaho)
    Libs.jade.forEach {
        implementation(it)
    }
    implementation(Libs.okHTTP)
    implementation(Libs.serialization)
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




tasks {
    dokka {
        moduleName = "annotations"
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"

        jdkVersion = 8

        // Specifies the location of the project source code on the Web.
        // If provided, Dokka generates "source" links for each declaration.
        // Repeat for multiple mappings
        linkMapping {
            // Unix based directory relative path to the root of the project
            // (where you execute gradle respectively).
            dir = "src/main/kotlin"

            // URL showing where the source code can be accessed through the
            // web browser.
            url = "https://github.com/Seriyin/KnowLedger/blob/master/agent/src/main/kotlin"

            // Suffix which is used to append the line number to the URL.
            // Use #L for GitHub.
            suffix = "#L"
        }
    }


    withType<Test> {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

}
