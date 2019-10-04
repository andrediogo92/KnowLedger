package org.knowledger.plugin

import Plugins
import Versions
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File


internal fun Project.addBarebonesTasks() {
    val extension: DocsOnlyPluginExtension =
        extensions["docs"] as DocsOnlyPluginExtension
    addDokkaTask(extension)
    addKotlinTask(extension)
}

internal fun Project.addDokkaTask(
    extension: ModuleNameProvider
) {
    tasks.withType<DokkaTask> {
        configureDokka(extension, buildDir)
    }
}

private fun DokkaTask.configureDokka(
    extension: ModuleNameProvider, buildDir: File
) {
    moduleName = extension.module
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"

    jdkVersion = Versions.jdkV

    // Specifies the location of the project source code on the Web.
    // If provided, Dokka generates "source" links for each declaration.
    // Repeat for multiple mappings
    linkMapping {
        // Unix based directory relative path to the root of the project
        // (where you execute gradle respectively).
        dir = "src/main/kotlin"

        // URL showing where the source code can be accessed through the
        // web browser.
        url = "https://github.com/Seriyin/KnowLedger/blob/master/${moduleName}/src/main/kotlin"

        // Suffix which is used to append the line number to the URL.
        // Use #L for GitHub.
        suffix = "#L"
    }
}

fun Project.addKotlinTask(extension: HasInlineClasses) {
    tasks.withType<KotlinCompile> {
        if (extension.inlineClasses) {
            kotlinOptions.freeCompilerArgs += "-XXLanguage:+InlineClasses"
        }
        kotlinOptions.jvmTarget = Versions.jdk
    }

}


internal fun Project.addBarebonesDependencies() {
    dependencies {
        implementation(kotlin("stdlib", Versions.kotlin))
    }
}

internal fun Project.addDocsPlugin() {
    plugins.apply(Plugins.dokka)
}

internal fun DependencyHandlerScope.implementation(
    dependencyNotation: Any
): Dependency? =
    add("implementation", dependencyNotation)

internal fun DependencyHandlerScope.testImplementation(
    dependencyNotation: Any
): Dependency? =
    add("testImplementation", dependencyNotation)

internal fun DependencyHandlerScope.testRuntimeOnly(
    dependencyNotation: Any
): Dependency? =
    add("testRuntimeOnly", dependencyNotation)