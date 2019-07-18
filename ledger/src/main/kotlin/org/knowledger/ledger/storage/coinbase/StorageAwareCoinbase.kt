package org.knowledger.ledger.storage.coinbase

import com.squareup.moshi.JsonClass
import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageID
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.StorageAware
import org.tinylog.kotlin.Logger

@JsonClass(generateAdapter = true)
internal data class StorageAwareCoinbase(
    internal val coinbase: StorageUnawareCoinbase
) : Coinbase by coinbase, StorageAware<Coinbase> {
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
        container: LedgerContainer
    ) : this(StorageUnawareCoinbase(container))
}