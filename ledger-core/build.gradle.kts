import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "0.1"


plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.noarg")
    id("org.jetbrains.dokka")
}


repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib", Versions.kotlin))
    Libs.orientDB.forEach {
        implementation(it)
    }
    implementation(Libs.bouncyCastle)
    implementation(Libs.jol)
    Libs.tinylog.forEach {
        implementation(it)
    }

    testImplementation(Libs.assertK)
    testImplementation(Libs.commonsRNG)
    testImplementation(Libs.jUnitApi)
    Libs.jUnitRuntime.forEach {
        testRuntimeOnly(it)
    }
}

tasks {
    dokka {
        moduleName = "ledger-core"
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
            url = "https://github.com/Seriyin/KnowLedger/blob/master/ledger-core/src/main/kotlin"

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
        kotlinOptions.freeCompilerArgs += "-XXLanguage:+InlineClasses"
        kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.Experimental"
        kotlinOptions.jvmTarget = "1.8"
    }
}


/**
 * Setup to generate testing artifacts for consumption by
 * other modules. This allows ledger or agent tests
 * to depend on common utility functions.
 */
val testJar by tasks.registering(Jar::class) {
    archiveClassifier.set("tests")
    from(project.the<SourceSetContainer>()["test"].output)
}

// Create a configuration for runtime
val testRuntimeElements: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class, Usage.JAVA_RUNTIME_JARS))
    }
    outgoing {
        // Indicate a different capability (defaults to group:name:version)
        capability("org.knowledger.ledger.core:test:$version")
    }
}

// Second configuration declaration, this is because of the API vs runtime difference Gradle 
// makes and rules around valid multiple variant selection
val testApiElements: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        // API instead of runtime usage
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class, Usage.JAVA_API_JARS))
    }
    outgoing {
        // Same capability
        capability("org.knowledger.ledger.core:test:$version")
    }
}

artifacts.add(testRuntimeElements.name, testJar)
artifacts.add(testApiElements.name, testJar)