package org.knowledger.common.database.orient

import com.orientechnologies.orient.core.id.ORID
import org.knowledger.common.database.StorageBytes
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageID

data class DocumentID internal constructor(
    internal val id: ORID
) : ORID by id, StorageID {
    override val element: StorageElement
        get() = DocumentElement(id.getRecord())
    override val bytes: StorageBytes
        get() = DocumentBytes(id.getRecord())
}