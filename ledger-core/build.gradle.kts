import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "0.1"


plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.noarg")
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