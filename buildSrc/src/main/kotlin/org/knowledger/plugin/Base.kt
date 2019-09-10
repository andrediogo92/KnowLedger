@file:Suppress("UnstableApiUsage")

package org.knowledger.plugin

import Libs
import Plugins
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType

internal fun Project.addCommonPlugins() {
    addDocsPlugin()
    plugins.apply(Plugins.serialization)
}

internal fun Project.addCommonTasks() {
    val extension: BaseJVMPluginExtension =
        extensions["baseJVM"] as BaseJVMPluginExtension
    addDokkaTask(extension)
    addKotlinTask(extension)
    with(tasks) {

        withType<Test> {
            useJUnitPlatform {
                includeEngines("junit-jupiter")
            }
        }
    }
}

internal fun Project.addCommonDependencies() {
    addBarebonesDependencies()
    dependencies {
        implementation(Libs.serialization)

        Libs.tinylog.forEach {
            implementation(it)
        }

        testImplementation(Libs.assertK)
        testImplementation(Libs.jUnitApi)
        Libs.jUnitRuntime.forEach {
            testRuntimeOnly(it)
        }
    }
}