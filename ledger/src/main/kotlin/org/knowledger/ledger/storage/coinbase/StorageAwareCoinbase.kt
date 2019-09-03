package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.data.DataFormula
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.tinylog.kotlin.Logger

@Serializable
@SerialName("StorageCoinbaseWrapper")
internal data class StorageAwareCoinbase(
    internal val coinbase: HashedCoinbaseImpl
) : HashedCoinbase by coinbase, StorageAware<HashedCoinbase> {
    override fun update(session: NewInstanceSession): Outcome<StorageID, UpdateFailure> {
        Logger.warn("Coinbase IS NOT update ready!! -> NOOP")
        return Outcome.Ok(id!!)
    }

    override val invalidated: Map<String, Any>
        get() = invalidatedFields

    @Transient
    override var id: StorageID? = null

    @Transient
    private var invalidatedFields =
        mutableMapOf<String, Any>()

    internal constructor(
        difficulty: Difficulty,
        blockheight: Long,
        container: LedgerContainer
    ) : this(
        HashedCoinbaseImpl(
            difficulty, blockheight,
            container
        )
    )

    constructor(
        difficulty: Difficulty, blockheight: Long,
        coinbaseParams: CoinbaseParams,
        dataFormula: DataFormula,
        cbor: Cbor, hasher: Hashers
    ) : this(
        HashedCoinbaseImpl(
            difficulty, blockheight,
            coinbaseParams, hasher, cbor, dataFormula
        )
    )
}