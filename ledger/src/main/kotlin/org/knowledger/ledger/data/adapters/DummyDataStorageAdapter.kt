package org.knowledger.ledger.data.adapters

import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.adapters.AbstractStorageAdapter
import org.knowledger.ledger.core.storage.results.DataFailure
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.DummyData

object DummyDataStorageAdapter : AbstractStorageAdapter<DummyData>(
    DummyData::class.java,
    Hashers.SHA3512Hasher
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "origin" to StorageType.INTEGER
        )

    override fun store(
        toStore: LedgerData, session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(id)
            .setStorageProperty(
                "origin",
                0xCC.toByte()
            )


    override fun load(
        element: StorageElement
    ): Outcome<DummyData, DataFailure> =
        commonLoad(element, id) {
            Outcome.Ok(
                DummyData
            )
        }
}