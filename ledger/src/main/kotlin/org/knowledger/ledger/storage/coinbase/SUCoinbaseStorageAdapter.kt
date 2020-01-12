package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.allValues
import org.knowledger.ledger.results.flatMapSuccess
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter

internal class SUCoinbaseStorageAdapter(
    private val container: LedgerInfo,
    private val transactionOutputStorageAdapter: TransactionOutputStorageAdapter
) : LedgerStorageAdapter<HashedCoinbaseImpl> {
    override val id: String
        get() = "Coinbase"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "payoutTXOs" to StorageType.SET,
            "payout" to StorageType.PAYOUT,
            "hash" to StorageType.HASH,
            "difficulty" to StorageType.DIFFICULTY,
            "blockheight" to StorageType.LONG,
            "extraNonce" to StorageType.LONG
        )

    override fun store(
        toStore: HashedCoinbaseImpl, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setElementSet(
                "payoutTXOs",
                toStore
                    .transactionOutputs
                    .map {
                        transactionOutputStorageAdapter.persist(
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
                .map {
                    transactionOutputStorageAdapter.load(
                        ledgerHash, it
                    )
                }.allValues()
                .flatMapSuccess { txos ->
                    Outcome.Ok(
                        HashedCoinbaseImpl(
                            txos.toMutableSet(),
                            element.getPayoutProperty("payout"),
                            difficulty, blockheight, extraNonce,
                            container.coinbaseParams, container.formula,
                            element.getHashProperty("hash"),
                            container.hasher, container.encoder
                        )
                    )
                }
        }

}