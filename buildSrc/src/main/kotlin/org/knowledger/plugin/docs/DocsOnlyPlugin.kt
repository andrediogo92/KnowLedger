package org.knowledger.plugin.docs

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.knowledger.plugin.addBarebonesDependencies
import org.knowledger.plugin.addBarebonesTasks
import org.knowledger.plugin.addDocsPlugin

open class DocsOnlyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val key = "docsPlugin"
        with(target) {
            target.extensions.create(key, DocsOnlyPluginExtension::class.java)
            addDocsPlugin()
            addBarebonesDependencies()
            afterEvaluate {
                addBarebonesTasks(key)
            }
        }
    }
}
