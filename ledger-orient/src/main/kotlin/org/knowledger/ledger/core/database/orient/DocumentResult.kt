package org.knowledger.ledger.core.database.orient

import com.orientechnologies.orient.core.sql.executor.OResult
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageResult

internal inline class DocumentResult constructor(
    internal val result: OResult
) : StorageResult {
    override val element: StorageElement
        get() = DocumentElement(result.toElement())
}