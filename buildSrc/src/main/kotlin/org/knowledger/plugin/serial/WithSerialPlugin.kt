package org.knowledger.plugin.serial

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.knowledger.plugin.addCommonTasks
import org.knowledger.plugin.addSerialDependencies
import org.knowledger.plugin.addSerialPlugins

open class WithSerialPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val key = "serialPlugin"
        with(target) {
            target.extensions.create(key, WithSerialPluginExtension::class.java)
            addSerialPlugins()
            addSerialDependencies()
            afterEvaluate {
                addCommonTasks(key)
            }
        }
    }
}