package pt.um.masb.common.database.orient

import com.orientechnologies.orient.core.sql.executor.OResult
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageResult

internal inline class DocumentResult constructor(
    internal val result: OResult
) : StorageResult {
    override val element: StorageElement
        get() = DocumentElement(result.toElement())
}