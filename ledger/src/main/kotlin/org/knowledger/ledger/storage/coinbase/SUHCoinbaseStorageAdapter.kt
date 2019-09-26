package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.allValues
import org.knowledger.ledger.core.results.mapSuccess
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.CoinbaseStorageAdapter
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter

internal object SUHCoinbaseStorageAdapter : LedgerStorageAdapter<HashedCoinbaseImpl> {
    override val id: String
        get() = CoinbaseStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = CoinbaseStorageAdapter.properties

    override fun store(
        toStore: HashedCoinbaseImpl, session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(CoinbaseStorageAdapter.id)
            .setElementSet(
                "payoutTXOs",
                toStore
                    .transactionOutputs
                    .asSequence()
                    .map {
                        TransactionOutputStorageAdapter.store(
                            it, session
                        )
                    }.toSet()
            ).setDifficultyProperty(
                "difficulty", toStore.difficulty, session
            ).setStorageProperty("blockheight", toStore.blockheight)
            .setStorageProperty("extraNonce", toStore.extraNonce)
            .setPayoutProperty("payout", toStore.payout)
            .setHashProperty("hash", toStore.hash)

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<HashedCoinbaseImpl, LoadFailure> =
        tryOrLoadUnknownFailure {
            val difficulty =
                element.getDifficultyProperty("difficulty")

            val blockheight: Long =
                element.getStorageProperty("blockheight")

            val extraNonce: Long =
                element.getStorageProperty("extraNonce")

            element
                .getElementSet("payoutTXOs")
                .asSequence()
                .map {
                    TransactionOutputStorageAdapter.load(ledgerHash, it)
                }.allValues()
                .mapSuccess { txos ->
                    LedgerHandle.getContainer(ledgerHash)!!.let {
                        HashedCoinbaseImpl(
                            txos.toMutableSet(),
                            element.getPayoutProperty("payout"),
                            difficulty,
                            blockheight,
                            extraNonce,
                            it.coinbaseParams,
                            it.formula,
                            element.getHashProperty("hash"),
                            it.hasher,
                            it.cbor
                        )
                    }
                }
        }
}