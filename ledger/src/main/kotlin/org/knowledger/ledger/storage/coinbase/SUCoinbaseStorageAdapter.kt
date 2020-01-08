package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.allValues
import org.knowledger.ledger.results.flatMapSuccess
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.CoinbaseStorageAdapter
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.loadTransactionOutput
import org.knowledger.ledger.storage.adapters.persist

internal object SUCoinbaseStorageAdapter : LedgerStorageAdapter<HashedCoinbaseImpl> {
    override val id: String
        get() = CoinbaseStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = CoinbaseStorageAdapter.properties

    override fun store(
        toStore: HashedCoinbaseImpl, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(CoinbaseStorageAdapter.id)
            .setElementSet(
                "payoutTXOs",
                toStore
                    .transactionOutputs
                    .map {
                        it.persist(session)
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
                .map {
                    it.loadTransactionOutput(ledgerHash)
                }.allValues()
                .flatMapSuccess { txos ->
                    val container = LedgerHandle.getContainer(ledgerHash)
                    container?.let {
                        Outcome.Ok(
                            HashedCoinbaseImpl(
                                txos.toMutableSet(),
                                element.getPayoutProperty("payout"),
                                difficulty, blockheight, extraNonce,
                                it.coinbaseParams, it.formula,
                                element.getHashProperty("hash"),
                                it.hasher, it.encoder
                            )
                        )
                    } ?: Outcome.Error(
                        LoadFailure.NoMatchingContainer(ledgerHash)
                    )
                }
        }
}