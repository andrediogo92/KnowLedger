package pt.um.masb.common.database.orient

import com.orientechnologies.orient.core.record.impl.OBlob
import pt.um.masb.common.database.StorageBytes

data class DocumentBytes internal constructor(
    internal val blob: OBlob
) : StorageBytes, OBlob by blob {

    override val bytes: ByteArray
        get() = blob.toStream()
}

