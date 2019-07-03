package org.knowledger.common.database.orient

import com.orientechnologies.orient.core.metadata.schema.OProperty
import org.knowledger.common.database.SchemaProperty

data class DocumentProperty internal constructor(
    internal val property: OProperty
) : SchemaProperty