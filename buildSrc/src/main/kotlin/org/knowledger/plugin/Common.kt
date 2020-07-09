package org.knowledger.plugin

import Libs
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.knowledger.plugin.base.BaseJVMPluginExtension

internal fun Project.addCommonPlugins() {
    addDocsPlugin()
}

internal fun Project.addCommonTasks(key: String) {
    val extension: BaseJVMPluginExtension =
        extensions[key] as BaseJVMPluginExtension
    with(tasks) {
        addDokkaTask(extension, buildDir)
        applyJunit()
        onKotlinCompile {
            configureKotlin(extension)
            applyOptIn(extension)
        }
    }
}

private fun KotlinCompile.applyOptIn(extension: OptIn) {
    if (extension.experimentalOptIn) {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.Experimental"
    }
    if (extension.requiresOptIn) {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
    if (extension.experimentalContracts) {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.contracts.ExperimentalContracts"
    }
}

private fun TaskContainer.applyJunit() {
    withType<Test> {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
    }
}


internal fun Project.addCommonDependencies() {
    addBarebonesDependencies()
    dependencies {
        Libs.tinylog.forEach(::implementation)

        testImplementation(Libs.assertK)
        testImplementation(Libs.jUnitApi)
        Libs.jUnitRuntime.forEach(::testRuntimeOnly)
    }
}