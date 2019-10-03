package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.data.DataFormula
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.tinylog.kotlin.Logger

@Serializable
@SerialName("StorageCoinbaseWrapper")
internal data class StorageAwareCoinbase(
    internal val coinbase: HashedCoinbaseImpl
) : HashedCoinbase by coinbase,
    StorageAware<HashedCoinbase> {
    override val invalidated: List<StoragePairs>
        get() = invalidatedFields

    @Transient
    override var id: StorageID? = null

    @Transient
    private var invalidatedFields =
        mutableListOf<StoragePairs>()


    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> {
        Logger.warn("Coinbase IS NOT update ready!! -> NOOP")
        return Outcome.Ok(id!!)
    }

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

    constructor(
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