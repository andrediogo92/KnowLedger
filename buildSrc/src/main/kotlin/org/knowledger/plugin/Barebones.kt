package org.knowledger.plugin

import Plugins
import Versions
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.knowledger.plugin.docs.DocsOnlyPluginExtension
import java.io.File


internal fun Project.addBarebonesTasks(key: String) {
    val extension: DocsOnlyPluginExtension =
        extensions[key] as DocsOnlyPluginExtension
    with(tasks) {
        addDokkaTask(extension, buildDir)
        onKotlinCompile {
            configureKotlin(extension)
        }
    }
}

inline fun TaskContainer.onKotlinCompile(
    crossinline function: KotlinCompile.() -> Unit
) {
    withType<KotlinCompile> {
        function()
    }
}

internal fun TaskContainer.addDokkaTask(
    extension: ModuleNameProvider,
    buildDir: File
) {
    withType<DokkaTask> {
        configureDokka(extension, buildDir)
    }
}

private fun DokkaTask.configureDokka(
    extension: ModuleNameProvider, buildDir: File
) {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"

    configuration {
        moduleName = extension.module

        jdkVersion = Versions.jdkV

        // Specifies the location of the project source code on the Web.
        // If provided, Dokka generates "source" links for each declaration.
        // Repeat for multiple mappings
        sourceLink {
            // Unix based directory relative path to the root of the project
            // (where you execute gradle respectively).
            path = "src/main/kotlin"

            // URL showing where the source code can be accessed through the
            // web browser.
            url = "https://github.com/Seriyin/KnowLedger/blob/master/${moduleName}/src/main/kotlin"

            // Suffix which is used to append the line number to the URL.
            // Use #L for GitHub.
            lineSuffix = "#L"
        }
    }
}

fun KotlinCompile.configureKotlin(extension: HasInlineClasses) {
    if (extension.inlineClasses) {
        kotlinOptions.freeCompilerArgs += "-XXLanguage:+InlineClasses"
    }
    kotlinOptions.jvmTarget = Versions.jdk
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