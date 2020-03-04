package org.knowledger.plugin

import Libs
import Plugins
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.addSerialPlugins() {
    addCommonPlugins()
    plugins.apply(Plugins.serialization)
}

internal fun Project.addSerialDependencies() {
    addCommonDependencies()
    dependencies {
        Libs.serialization.forEach(::implementation)
    }
}