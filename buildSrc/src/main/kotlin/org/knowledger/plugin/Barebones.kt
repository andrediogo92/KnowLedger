package org.knowledger.plugin

import Versions
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
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
//    named("dokkaHtml", DokkaTask::class.java).configure {
//        configureDokka(extension, buildDir)
//    }
}

/*
private fun DokkaTask.configureDokka(
    extension: ModuleNameProvider, buildDir: File
) {

    outputDirectory = "$buildDir/javadoc"

    dokkaSourceSets {
        configureEach {
            moduleDisplayName = extension.module

            // List of files with module and package documentation
            // https://kotlinlang.org/docs/reference/kotlin-doc.html#module-and-package-documentation
            includes = listOf("packages.md", "extra.md")

            // List of files or directories containing sample code (referenced with @sample tags)
            samples = listOf("samples/basic.kt", "samples/advanced.kt")
        }

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
*/

fun KotlinCompile.configureKotlin(extension: HasInlineClasses) {
    if (extension.inlineClasses) {
        kotlinOptions.freeCompilerArgs += "-XXLanguage:+InlineClasses"
    }
    kotlinOptions.jvmTarget = Versions.jdk
}



internal fun Project.addDocsPlugin() {
//    plugins.apply(Plugins.dokka)
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