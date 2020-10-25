package org.knowledger.database.orient

import com.orientechnologies.orient.core.sql.executor.OResult
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageResult

internal inline class DocumentResult(
    internal val result: OResult
) : StorageResult {
    override val element: StorageElement get() = DocumentElement(result.toElement())
}