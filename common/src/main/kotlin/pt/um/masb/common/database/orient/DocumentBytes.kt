package pt.um.masb.common.database.orient

import com.orientechnologies.orient.core.record.impl.OBlob
import pt.um.masb.common.database.StorageBytes
import java.io.ByteArrayOutputStream

data class DocumentBytes internal constructor(
    internal val blob: OBlob
) : StorageBytes, OBlob by blob {

    override val bytes: ByteArray
        get() = blob.toStream()

    override fun toOutputStream(bos: ByteArrayOutputStream) =
        blob.toOutputStream(bos)

    override fun discard(): StorageBytes =
        apply {
            blob.unload<OBlob>()
        }
}

