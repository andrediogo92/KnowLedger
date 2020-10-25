package org.knowledger.plugin

open class PluginConfigurationExtension(
    var packageName: String = "org.knowledger",
    var module: String = "",
    var isLibrary: Boolean = true,
    var inlineClasses: Boolean = false,
    var experimentalContracts: Boolean = false,
    var experimentalOptIn: Boolean = false,
    var requiresOptIn: Boolean = false
)