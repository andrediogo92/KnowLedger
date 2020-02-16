@file:UseSerializers(TransactionByteSerializer::class)

package org.knowledger.ledger.service.pools.transaction

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.serial.binary.TransactionByteSerializer
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.updateLinked

@Serializable
internal class PoolTransaction private constructor(
    val transaction: Transaction,
    val confirmed: Boolean = false
) : ServiceClass, StorageAware<PoolTransaction> {

    internal constructor(
        adapterManager: AdapterManager,
        transaction: Transaction,
        confirmed: Boolean = false
    ) : this(transaction, confirmed) {
        pInvalidated = arrayOf(
            StoragePairs.Linked("transaction", adapterManager.transactionStorageAdapter),
            StoragePairs.Native("confirmed")
        )
    }

    @Transient
    override var id: StorageID? = null

    @Transient
    private var pInvalidated: Array<StoragePairs<*>> = emptyArray()

    override val invalidated: Array<StoragePairs<*>>
        get() = pInvalidated

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        updateLinked(session, invalidated)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PoolTransaction

        if (transaction != other.transaction) return false
        if (confirmed != other.confirmed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transaction.hashCode()
        result = 31 * result + confirmed.hashCode()
        return result
    }

}