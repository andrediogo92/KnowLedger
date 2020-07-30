package org.knowledger.ledger.data.adapters

import kotlinx.serialization.KSerializer
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.DummyData
import org.knowledger.ledger.database.NewInstanceSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.serial.DummyDataSerializer
import org.knowledger.ledger.storage.LedgerData

internal class DummyDataStorageAdapter(hasher: Hashers) : AbstractStorageAdapter<DummyData>(
    DummyData::class.java, hasher
) {
    override val serializer: KSerializer<DummyData>
        get() = DummyDataSerializer

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "origin" to StorageType.INTEGER
        )

    override fun store(
        toStore: LedgerData, session: NewInstanceSession
    ): StorageElement = session.newInstance(id)
        .setStorageProperty("origin", 0xCC.toByte())


    override fun load(
        element: StorageElement
    ): Outcome<DummyData, DataFailure> =
        commonLoad(element, id) { DummyData.ok() }
}