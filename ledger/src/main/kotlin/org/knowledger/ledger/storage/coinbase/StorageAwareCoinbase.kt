package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Hashers
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.replace
import org.knowledger.ledger.storage.simpleUpdate

internal data class StorageAwareCoinbase(
    internal val coinbase: HashedCoinbaseImpl
) : HashedCoinbase by coinbase,
    StorageAware<HashedCoinbase> {
    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(
            StoragePairs.Native("extraNonce"),
            StoragePairs.Hash("hash")
        )

    override var id: StorageID? = null

    override fun newNonce() {
        coinbase.newNonce()
        invalidated.replace(0, extraNonce)
        invalidated.replace(1, hash)
    }

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidated)

    internal constructor(
        difficulty: Difficulty,
        blockheight: Long,
        container: LedgerContainer
    ) : this(
        coinbase = HashedCoinbaseImpl(
            difficulty = difficulty, blockheight = blockheight,
            container = container
        )
    )

    internal constructor(
        difficulty: Difficulty, blockheight: Long,
        coinbaseParams: CoinbaseParams,
        dataFormula: DataFormula,
        encoder: BinaryFormat, hasher: Hashers
    ) : this(
        coinbase = HashedCoinbaseImpl(
            difficulty = difficulty, blockheight = blockheight,
            coinbaseParams = coinbaseParams, dataFormula = dataFormula,
            hasher = hasher, encoder = encoder
        )
    )
}