package org.knowledger.ledger.adapters.storage

import com.github.michaelbull.result.map
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.service.PersistenceContext
import org.knowledger.ledger.service.solver.StorageSolver
import org.knowledger.ledger.service.solver.pushNewDifficulty
import org.knowledger.ledger.service.solver.pushNewHash
import org.knowledger.ledger.service.solver.pushNewLinked
import org.knowledger.ledger.service.solver.pushNewNative
import org.knowledger.ledger.service.solver.pushNewPayout
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure

internal class CoinbaseHeaderStorageAdapter : LedgerStorageAdapter<MutableCoinbaseHeader> {
    override val id: String
        get() = "CoinbaseHeader"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "hash" to StorageType.HASH,
            "merkleRoot" to StorageType.HASH,
            "payout" to StorageType.PAYOUT,
            "blockheight" to StorageType.LONG,
            "difficulty" to StorageType.DIFFICULTY,
            "extraNonce" to StorageType.LONG,
            "coinbaseParams" to StorageType.LINK
        )

    override fun store(
        element: MutableCoinbaseHeader, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewHash("hash", element.hash)
                pushNewHash("merkleRoot", element.merkleRoot)
                pushNewPayout("payout", element.payout)
                pushNewNative("blockheight", element.blockheight)
                pushNewDifficulty("difficulty", element.difficulty)
                pushNewNative("extraNonce", element.extraNonce)
                pushNewLinked("coinbaseParams", element.coinbaseParams, AdapterIds.CoinbaseParams)
            }.ok()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<MutableCoinbaseHeader, LoadFailure> =
        element.cachedLoad {
            tryOrLoadUnknownFailure {
                val params =
                    element.getLinked("coinbaseParams")
                context.coinbaseParamsStorageAdapter.load(
                    ledgerHash, params, context
                ).map { coinbaseParams ->
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
                    context.coinbaseHeaderFactory.create(
                        hash, merkleRoot, payout, blockheight,
                        difficulty, extraNonce, coinbaseParams
                    )
                }
            }
        }
}