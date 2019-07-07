package org.knowledger.ledger.data.adapters

import org.knowledger.common.data.LedgerData
import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.results.Outcome
import org.knowledger.common.storage.adapters.AbstractStorageAdapter
import org.knowledger.common.storage.results.DataFailure
import org.knowledger.ledger.data.DummyData

object DummyDataStorageAdapter : AbstractStorageAdapter<DummyData>(
    DummyData::class.java
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
                DummyData.DUMMY
            )
        }
}