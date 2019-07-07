package org.knowledger.ledger.data.adapters

import org.knowledger.common.data.LedgerData
import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageBytes
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageID
import org.knowledger.common.database.StorageType
import org.knowledger.common.results.Outcome
import org.knowledger.common.storage.adapters.AbstractStorageAdapter
import org.knowledger.common.storage.results.DataFailure
import org.knowledger.ledger.data.OtherData
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

object OtherDataStorageAdapter : AbstractStorageAdapter<OtherData>(
    OtherData::class.java
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "value" to StorageType.LINK
        )

    override fun store(
        toStore: LedgerData, session: NewInstanceSession
    ): StorageElement =
        (toStore as OtherData).let { odata ->
            session.newInstance(id).apply {
                val byteStream = ByteArrayOutputStream(
                    odata.approximateSize.toInt()
                )
                ObjectOutputStream(
                    byteStream
                ).use {
                    it.writeObject(odata.data)
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
                setStorageProperty("value", blobs)
            }
        }


    override fun load(element: StorageElement): Outcome<OtherData, DataFailure> =
        commonLoad(element, id) {
            val bos = ByteArrayOutputStream()
            val chunkIds: List<StorageID> =
                getStorageIDs("value")
            for (id in chunkIds) {
                val chunk = id.bytes
                chunk.toOutputStream(bos)
                chunk.discard()
            }

            ObjectInputStream(
                ByteArrayInputStream(
                    bos.toByteArray()
                )
            ).use {
                Outcome.Ok(
                    OtherData(
                        it.readObject() as Serializable
                    )
                )
            }
        }
}