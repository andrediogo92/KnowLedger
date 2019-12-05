package org.knowledger.plugin.base

import org.knowledger.plugin.IncludedInPackage
import org.knowledger.plugin.IsLibrary
import org.knowledger.plugin.docs.DocsOnlyPluginExtension

open class BaseJVMPluginExtension(
    override var packageName: String = "org.knowledger",
    override var library: Boolean = true
) : DocsOnlyPluginExtension(), IncludedInPackage, IsLibrary