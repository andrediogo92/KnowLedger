package org.knowledger.plugin.serial

import Libs
import Plugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.knowledger.plugin.base.BaseConfigurationPlugin
import org.knowledger.plugin.implementation

open class SerialConfigurationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.apply(BaseConfigurationPlugin::class)
            plugins.apply(Plugins.serialization)
            addSerialDependencies()
        }
    }

    private fun Project.addSerialDependencies() {
        dependencies {
            Libs.serialization.forEach(this::implementation)
        }
    }
}