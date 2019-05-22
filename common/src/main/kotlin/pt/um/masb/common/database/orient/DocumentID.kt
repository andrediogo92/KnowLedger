package pt.um.masb.common.database.orient

import com.orientechnologies.orient.core.id.ORID
import pt.um.masb.common.database.StorageBytes
import pt.um.masb.common.database.StorageID

internal inline class DocumentID(internal val id: ORID) : StorageID {
    override fun getBytes(): StorageBytes =
        DocumentBytes(id.getRecord())
}