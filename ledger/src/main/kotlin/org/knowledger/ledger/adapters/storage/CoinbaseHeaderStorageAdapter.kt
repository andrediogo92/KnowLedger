package org.knowledger.ledger.adapters.storage

import com.github.michaelbull.result.map
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.results.LoadFailure

internal class CoinbaseHeaderStorageAdapter : LedgerStorageAdapter<MutableCoinbaseHeader> {
    override val id: String get() = "CoinbaseHeader"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "hash" to StorageType.HASH,
            "merkleRoot" to StorageType.HASH,
            "payout" to StorageType.PAYOUT,
            "blockheight" to StorageType.LONG,
            "difficulty" to StorageType.DIFFICULTY,
            "extraNonce" to StorageType.LONG,
            "coinbaseParams" to StorageType.LINK,
        )

    override fun store(
        element: MutableCoinbaseHeader, state: StorageState,
    ): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewHash("hash", element.hash)
            pushNewHash("merkleRoot", element.merkleRoot)
            pushNewPayout("payout", element.payout)
            pushNewNative("blockheight", element.blockheight)
            pushNewDifficulty("difficulty", element.difficulty)
            pushNewNative("extraNonce", element.extraNonce)
            pushNewLinked("coinbaseParams", element.coinbaseParams, AdapterIds.CoinbaseParams)
        }.ok()

    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<MutableCoinbaseHeader, LoadFailure> =
        element.cachedLoad {
            val params = getLinked("coinbaseParams")
            context.coinbaseParamsStorageAdapter
                .load(ledgerHash, params, context)
                .map { coinbaseParams ->
                    val merkleRoot: Hash = getHashProperty("merkleRoot")
                    val payout: Payout = getPayoutProperty("payout")
                    val difficulty: Difficulty = getDifficultyProperty("difficulty")
                    val blockheight: Long = getStorageProperty("blockheight")
                    val extraNonce: Long = getStorageProperty("extraNonce")
                    val hash: Hash = getHashProperty("hash")
                    context.coinbaseHeaderFactory.create(
                        hash, merkleRoot, payout, blockheight,
                        difficulty, extraNonce, coinbaseParams,
                    )
                }
        }
}