package org.knowledger.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

open class BaseJVMPluginExtension(
    var packageName: String = "org.knowledger",
    var library: Boolean = true,
    override var inlineClasses: Boolean = false,
    override var module: String = ""
) : ModuleNameProvider, HasInlineClasses

open class BaseJVMPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            target.extensions.create("baseJVM", BaseJVMPluginExtension::class.java)
            addCommonPlugins()
            addCommonDependencies()
            addCommonTasks()
        }
    }
}
