package org.knowledger.ledger.storage.coinbase

import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.crypto.Hash
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
import org.knowledger.ledger.storage.adapters.WitnessStorageAdapter

internal class SUCoinbaseStorageAdapter(
    private val ledgerInfo: LedgerInfo,
    internal val witnessStorageAdapter: WitnessStorageAdapter
) : LedgerStorageAdapter<HashedCoinbaseImpl> {
    override val id: String
        get() = "Coinbase"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "witnesses" to StorageType.LIST,
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
            .setElementList(
                "witnesses",
                toStore
                    .witnesses
                    .map {
                        witnessStorageAdapter.persist(
                            it, session
                        )
                    }
            ).setPayoutProperty("payout", toStore.payout)
            .setHashProperty("hash", toStore.hash)
            .setDifficultyProperty(
                "difficulty", toStore.difficulty, session
            ).setStorageProperty(
                "blockheight", toStore.blockheight
            )
            .setStorageProperty(
                "extraNonce", toStore.extraNonce
            )

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
                .getElementList("witnesses")
                .map { element ->
                    witnessStorageAdapter.load(
                        ledgerHash, element
                    )
                }.allValues()
                .flatMapSuccess { witnesses ->
                    Outcome.Ok(
                        HashedCoinbaseImpl(
                            witnesses = witnesses.toMutableSortedListFromPreSorted(),
                            payout = element.getPayoutProperty("payout"),
                            difficulty = difficulty,
                            blockheight = blockheight,
                            extraNonce = extraNonce,
                            ledgerInfo = ledgerInfo,
                            hash = element.getHashProperty("hash")
                        )
                    )
                }
        }

}