package org.knowledger.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

open class DocsOnlyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            target.extensions.create("docs", DocsOnlyPluginExtension::class.java)
            addBarebonesTasks()
            addBarebonesDependencies()
            addDocsPlugin()
        }
    }
}

open class DocsOnlyPluginExtension(
    override var inlineClasses: Boolean = false,
    override var module: String = ""
) : ModuleNameProvider, HasInlineClasses
