package org.knowledger.plugin.base

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.knowledger.plugin.addCommonDependencies
import org.knowledger.plugin.addCommonPlugins
import org.knowledger.plugin.addCommonTasks

open class BaseJVMPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val key = "basePlugin"
        with(target) {
            target.extensions.create(key, BaseJVMPluginExtension::class.java)
            addCommonPlugins()
            addCommonDependencies()
            afterEvaluate {
                addCommonTasks(key)
            }
        }
    }
}
