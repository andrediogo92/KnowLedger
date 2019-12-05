@file:Suppress("UnstableApiUsage")

package org.knowledger.plugin

import Libs
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.knowledger.plugin.base.BaseJVMPluginExtension

internal fun Project.addCommonPlugins() {
    addDocsPlugin()
}

internal fun Project.addCommonTasks(key: String) {
    val extension: BaseJVMPluginExtension =
        extensions[key] as BaseJVMPluginExtension
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
        Libs.tinylog.forEach(::implementation)

        testImplementation(Libs.assertK)
        testImplementation(Libs.jUnitApi)
        Libs.jUnitRuntime.forEach(::testRuntimeOnly)
    }
}