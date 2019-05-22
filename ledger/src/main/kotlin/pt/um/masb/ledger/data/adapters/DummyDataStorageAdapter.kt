package pt.um.masb.ledger.data.adapters

import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.storage.adapters.AbstractStorageAdapter
import pt.um.masb.common.storage.results.DataResult
import pt.um.masb.ledger.data.DummyData

class DummyDataStorageAdapter : AbstractStorageAdapter<DummyData>(
    DummyData::class.java
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "origin" to StorageType.INTEGER
        )

    override fun store(
        toStore: BlockChainData, session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setStorageProperty(
                "origin",
                0xCC.toByte()
            )
        }


    override fun load(
        element: StorageElement
    ): DataResult<DummyData> =
        commonLoad(element, id) {
            DataResult.Success(
                DummyData.DUMMY
            )
        }
}