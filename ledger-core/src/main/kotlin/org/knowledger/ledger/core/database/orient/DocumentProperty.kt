package org.knowledger.ledger.core.database.orient

import com.orientechnologies.orient.core.metadata.schema.OProperty
import org.knowledger.ledger.core.database.SchemaProperty

data class DocumentProperty internal constructor(
    internal val property: OProperty
) : SchemaProperty