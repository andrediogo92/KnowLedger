package org.knowledger.plugin.docs

import org.knowledger.plugin.HasInlineClasses
import org.knowledger.plugin.ModuleNameProvider

open class DocsOnlyPluginExtension(
    override var inlineClasses: Boolean = false,
    override var module: String = ""
) : ModuleNameProvider, HasInlineClasses