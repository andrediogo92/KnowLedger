package org.knowledger.ledger.database.orient

import com.orientechnologies.orient.core.id.ORID
import org.knowledger.ledger.database.StorageBytes
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageID

data class DocumentID internal constructor(
    internal val id: ORID,
) : ORID by id, StorageID {
    override val element: StorageElement get() = DocumentElement(id.getRecord())
    override val bytes: StorageBytes get() = DocumentBytes(id.getRecord())
    override val key: String get() = id.toString()
}