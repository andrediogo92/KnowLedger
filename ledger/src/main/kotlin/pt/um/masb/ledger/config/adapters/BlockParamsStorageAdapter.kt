package pt.um.masb.ledger.config.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.ledger.config.BlockParams
import pt.um.masb.ledger.results.tryOrLoadQueryFailure
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.adapters.LedgerStorageAdapter

class BlockParamsStorageAdapter : LedgerStorageAdapter<BlockParams> {
    override val id: String
        get() = "BlockParams"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "blockMemSize" to StorageType.LONG,
            "blockLength" to StorageType.LONG
        )

    override fun store(
        toStore: BlockParams, session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setStorageProperty(
                "blockMemSize", toStore.blockMemSize
            )
            setStorageProperty(
                "blockLength", toStore.blockLength
            )
        }


    override fun load(
        hash: Hash, element: StorageElement
    ): LoadResult<BlockParams> =
        tryOrLoadQueryFailure {
            LoadResult.Success(
                BlockParams(
                    element.getStorageProperty("blockMemSize"),
                    element.getStorageProperty("blockLength")
                )
            )
        }

}