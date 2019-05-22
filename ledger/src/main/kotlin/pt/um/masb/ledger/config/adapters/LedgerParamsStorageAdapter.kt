package pt.um.masb.ledger.config.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.AvailableHashAlgorithms
import pt.um.masb.common.hash.Hash
import pt.um.masb.ledger.config.LedgerParams
import pt.um.masb.ledger.results.intoLoad
import pt.um.masb.ledger.results.tryOrLoadQueryFailure
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.adapters.LedgerStorageAdapter

class LedgerParamsStorageAdapter : LedgerStorageAdapter<LedgerParams> {
    val blockParamsStorageAdapter = BlockParamsStorageAdapter()

    override val id: String
        get() = "LedgerParams"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "crypter" to StorageType.BYTES,
            "recalcTime" to StorageType.LONG,
            "recalcTrigger" to StorageType.LONG,
            "blockParams" to StorageType.LINK
        )

    override fun store(
        toStore: LedgerParams, session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setHashProperty("crypter", toStore.crypter.id)
            setStorageProperty(
                "recalcTime", toStore.recalcTime
            )
            setStorageProperty(
                "recalcTrigger", toStore.recalcTrigger
            )
            setLinked(
                "blockParams", blockParamsStorageAdapter,
                toStore.blockParams, session
            )
        }


    override fun load(
        hash: Hash, element: StorageElement
    ): LoadResult<LedgerParams> =
        tryOrLoadQueryFailure {
            val crypterHash =
                element.getHashProperty("crypter")
            AvailableHashAlgorithms.getCrypter(crypterHash)?.let {
                val recalcTime: Long =
                    element.getStorageProperty("recalcTime")
                val recalcTrigger: Long =
                    element.getStorageProperty("recalcTrigger")
                val blockParams =
                    blockParamsStorageAdapter.load(
                        hash,
                        element.getLinked("blockParams")
                    )
                if (blockParams !is LoadResult.Success) {
                    return@tryOrLoadQueryFailure blockParams.intoLoad<LedgerParams>()
                }
                LoadResult.Success(
                    LedgerParams(
                        it, recalcTime,
                        recalcTrigger,
                        blockParams.data
                    )
                )
            } ?: LoadResult.NonMatchingCrypter<LedgerParams>(
                """Non matching crypter at load params:
                | with crypterHash: ${crypterHash.print}
                """.trimMargin()
            )
        }
}