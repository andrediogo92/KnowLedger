package org.knowledger.common.database.orient

import com.orientechnologies.orient.core.sql.executor.OResult
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageResult

internal inline class DocumentResult constructor(
    internal val result: OResult
) : StorageResult {
    override val element: StorageElement
        get() = DocumentElement(result.toElement())
}