package org.knowledger.plugin.base

import org.knowledger.plugin.IncludedInPackage
import org.knowledger.plugin.IsLibrary
import org.knowledger.plugin.OptIn
import org.knowledger.plugin.docs.DocsOnlyPluginExtension

open class BaseJVMPluginExtension(
    override var packageName: String = "org.knowledger",
    override var library: Boolean = true,
    override var experimentalOptIn: Boolean = false,
    override var requiresOptIn: Boolean = false
) : DocsOnlyPluginExtension(),
    IncludedInPackage,
    IsLibrary,
    OptIn