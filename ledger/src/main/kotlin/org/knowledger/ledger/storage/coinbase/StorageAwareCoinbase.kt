package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.replace
import org.knowledger.ledger.storage.simpleUpdate

internal data class StorageAwareCoinbase(
    internal val coinbase: HashedCoinbaseImpl
) : HashedCoinbase by coinbase,
    StorageAware<HashedCoinbase> {
    private var _invalidated: Array<StoragePairs<*>> =
        emptyArray()

    override val invalidated: Array<StoragePairs<*>>
        get() = _invalidated

    override var id: StorageID? = null

    internal constructor(
        info: LedgerInfo,
        adapterManager: AdapterManager
    ) : this(HashedCoinbaseImpl(info)) {
        _invalidated = arrayOf(
            StoragePairs.Native("extraNonce"),
            StoragePairs.Hash("hash"),
            StoragePairs.LinkedList(
                "witnesses",
                adapterManager.witnessStorageAdapter
            )
        )

    }

    override fun newNonce() {
        coinbase.newNonce()
        invalidated.replace(0, extraNonce)
        invalidated.replace(1, hash)
    }

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidated)

    override fun equals(other: Any?): Boolean =
        coinbase == other

    override fun hashCode(): Int =
        coinbase.hashCode()
}