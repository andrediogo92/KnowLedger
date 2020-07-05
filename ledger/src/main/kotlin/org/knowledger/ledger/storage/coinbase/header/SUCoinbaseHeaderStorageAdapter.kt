package org.knowledger.ledger.storage.coinbase.header

import org.knowledger.ledger.config.adapters.CoinbaseParamsStorageAdapter
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.intoLoad
import org.knowledger.ledger.results.mapFailure
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.coinbase.header.factory.CoinbaseHeaderFactory

internal class SUCoinbaseHeaderStorageAdapter(
    private val headerFactory: CoinbaseHeaderFactory
) : LedgerStorageAdapter<MutableHashedCoinbaseHeader> {
    private val coinbaseParamsStorageAdapter = CoinbaseParamsStorageAdapter

    override val id: String
        get() = "CoinbaseHeader"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "coinbaseParams" to StorageType.LINK,
            "merkleRoot" to StorageType.HASH,
            "payout" to StorageType.PAYOUT,
            "difficulty" to StorageType.DIFFICULTY,
            "blockheight" to StorageType.LONG,
            "extraNonce" to StorageType.LONG,
            "hash" to StorageType.HASH
        )

    override fun store(
        toStore: MutableHashedCoinbaseHeader, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setLinked(
                "coinbaseParams",
                coinbaseParamsStorageAdapter.persist(
                    toStore.coinbaseParams, session
                )
            ).setHashProperty("merkleRoot", toStore.merkleRoot)
            .setPayoutProperty("payout", toStore.payout)
            .setDifficultyProperty(
                "difficulty", toStore.difficulty, session
            ).setStorageProperty(
                "blockheight", toStore.blockheight
            )
            .setStorageProperty(
                "extraNonce", toStore.extraNonce
            ).setHashProperty("hash", toStore.hash)


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<MutableHashedCoinbaseHeader, LoadFailure> =
        tryOrLoadUnknownFailure {
            val params =
                element.getLinked("coinbaseParams")
            coinbaseParamsStorageAdapter.load(
                ledgerHash, params
            ).mapFailure {
                it.intoLoad()
            }.mapSuccess { coinbaseParams ->
                val merkleRoot: Hash =
                    element.getHashProperty("merkleRoot")
                val payout: Payout =
                    element.getPayoutProperty("payout")
                val difficulty: Difficulty =
                    element.getDifficultyProperty("difficulty")
                val blockheight: Long =
                    element.getStorageProperty("blockheight")
                val extraNonce: Long =
                    element.getStorageProperty("extraNonce")
                val hash: Hash =
                    element.getHashProperty("hash")
                headerFactory.create(
                    coinbaseParams = coinbaseParams,
                    merkleRoot = merkleRoot, payout = payout,
                    difficulty = difficulty, blockheight = blockheight,
                    extraNonce = extraNonce, hash = hash
                )
            }
        }

}