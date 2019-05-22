package pt.um.masb.ledger.data.adapters

import com.orientechnologies.orient.core.db.record.OIdentifiable
import com.orientechnologies.orient.core.record.impl.ORecordBytes
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageBytes
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.storage.adapters.AbstractStorageAdapter
import pt.um.masb.common.storage.results.DataResult
import pt.um.masb.ledger.data.OtherData
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class OtherDataStorageAdapter : AbstractStorageAdapter<OtherData>(
    OtherData::class.java
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "data" to StorageType.LINK
        )

    override fun store(
        toStore: BlockChainData, session: NewInstanceSession
    ): StorageElement {
        val otherData = toStore as OtherData
        return session.newInstance(id).apply {
            val byteStream = ByteArrayOutputStream(
                otherData.approximateSize.toInt()
            )
            ObjectOutputStream(
                byteStream
            ).use {
                it.writeObject(otherData.data)
            }
            val bytes = byteStream.toByteArray()
            val blobs = mutableListOf<StorageBytes>()
            var i = 0
            while (i + 2048 < bytes.size) {
                blobs.add(
                    session.newInstance(
                        bytes.sliceArray(i..i + 2048)
                    )
                )
                i += 2048
            }
            if (i <= bytes.size - 1) {
                blobs.add(
                    session.newInstance(
                        bytes.sliceArray(i until bytes.size)
                    )
                )
            }
            setStorageProperty("data", blobs)
        }
    }


    override fun load(
        element: StorageElement
    ): DataResult<OtherData> =
        commonLoad(element, id) {
            val bos = ByteArrayOutputStream()
            val chunkIds: List<OIdentifiable> =
                getStorageProperty("data")
            for (id in chunkIds) {
                val chunk = id.getRecord<ORecordBytes>()
                chunk.toOutputStream(bos)
                chunk.unload()
            }

            ObjectInputStream(
                ByteArrayInputStream(
                    bos.toByteArray()
                )
            ).use {
                DataResult.Success(
                    OtherData(
                        it.readObject() as Serializable
                    )
                )
            }
        }
}