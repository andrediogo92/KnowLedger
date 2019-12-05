package org.knowledger.ledger.database.orient

import com.orientechnologies.orient.core.metadata.schema.OProperty
import org.knowledger.ledger.database.SchemaProperty

data class DocumentProperty internal constructor(
    internal val property: OProperty
) : SchemaProperty