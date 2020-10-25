package org.knowledger.plugin.base

import Libs
import Plugins
import Versions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.knowledger.plugin.PluginConfigurationExtension
import org.knowledger.plugin.implementation
import org.knowledger.plugin.testImplementation
import org.knowledger.plugin.testRuntimeOnly
import java.io.File
import java.net.URL

class BaseConfigurationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val key = "pluginConfiguration"
        with(target) {
            target.extensions.create(key, PluginConfigurationExtension::class)
            addCommonDependencies()
            plugins.apply(Plugins.dokka)
            afterEvaluate {
                addBarebonesTasks(key)
            }
        }
    }

    private fun Project.addBarebonesTasks(key: String) {
        val extension = extensions.getByName<PluginConfigurationExtension>(key)
        with(tasks) {
            withType<DokkaTask>().configureEach {
                configureDokka(extension, buildDir)
            }
            withType<KotlinCompile> {
                configureKotlin(extension)
            }
            withType<Test> {
                useJUnitPlatform {
                    includeEngines("junit-jupiter")
                }
            }
        }
    }

    private fun DokkaTask.configureDokka(extension: PluginConfigurationExtension, buildDir: File) {
        outputDirectory.set(buildDir.resolve("dokka"))
        dokkaSourceSets {
            configureEach {
                moduleName.set(extension.module)

                // List of files with module and package documentation
                // https://kotlinlang.org/docs/reference/kotlin-doc.html#module-and-package-documentation
                includes.from(listOf("packages.md", "extra.md"))

                // List of files or directories containing sample code (referenced with @sample tags)
                //samples = listOf("samples/basic.kt", "samples/advanced.kt")

                jdkVersion.set(Versions.jdkV)

                // Specifies the location of the project source code on the Web.
                // If provided, Dokka generates "source" links for each declaration.
                // Repeat for multiple mappings
                sourceLink {
                    // Unix based directory relative path to the root of the project
                    // (where you execute gradle respectively).
                    localDirectory.set(File("src/main/kotlin"))

                    // URL showing where the source code can be accessed through the
                    // web browser.
                    remoteUrl.set(
                        URL("https://github" +
                            ".com/Seriyin/KnowLedger/blob/master/${extension.module}/src/main/kotlin"))

                    // Suffix which is used to append the line number to the URL.
                    // Use #L for GitHub.
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }

    private fun KotlinCompile.configureKotlin(extension: PluginConfigurationExtension) {
        if (extension.inlineClasses) {
            kotlinOptions.freeCompilerArgs += "-XXLanguage:+InlineClasses"
        }
        if (extension.experimentalOptIn) {
            kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.Experimental"
        }
        if (extension.requiresOptIn) {
            kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
        if (extension.experimentalContracts) {
            kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.contracts.ExperimentalContracts"
        }
        kotlinOptions.jvmTarget = Versions.jdk
    }

    private fun Project.addCommonDependencies() {
        dependencies {
            Libs.tinylog.forEach(this::implementation)
            testImplementation(Libs.assertK)
            testImplementation(Libs.jUnitApi)
            Libs.jUnitRuntime.forEach(this::testRuntimeOnly)
        }
    }
}
