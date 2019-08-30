import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.dokka")
}

version = "0.1"


dependencies {
    implementation(project(":ledger-core"))

    implementation(kotlin("stdlib", Versions.kotlin))
    implementation(Libs.bouncyCastle)
    implementation(Libs.serialization)

    testImplementation(project(":ledger-core")) {
        capabilities {
            // Indicate we want a variant with a specific capability
            requireCapability("org.knowledger.ledger.core:test")
        }
    }
    testImplementation(Libs.commonsRNG)
    testImplementation(Libs.assertK)
    testImplementation(Libs.jUnitApi)
    Libs.tinylog.forEach {
        testImplementation(it)
    }
    Libs.jUnitRuntime.forEach {
        testRuntimeOnly(it)
    }
}

tasks {
    dokka {
        moduleName = "ledger-crypto"
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
            url = "https://github.com/Seriyin/KnowLedger/blob/master/ledger-crypto/src/main/kotlin"

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