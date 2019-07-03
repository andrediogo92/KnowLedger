package pt.um.masb.common.database.orient

import com.orientechnologies.orient.core.id.ORID
import pt.um.masb.common.database.StorageBytes
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageID

data class DocumentID internal constructor(
    internal val id: ORID
) : ORID by id, StorageID {
    override val element: StorageElement
        get() = DocumentElement(id.getRecord())
    override val bytes: StorageBytes
        get() = DocumentBytes(id.getRecord())
}