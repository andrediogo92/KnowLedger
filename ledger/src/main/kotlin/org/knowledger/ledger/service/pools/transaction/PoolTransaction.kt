@file:UseSerializers(TransactionByteSerializer::class)

package org.knowledger.ledger.service.pools.transaction

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.serial.internal.TransactionByteSerializer
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.adapters.TransactionStorageAdapter
import org.knowledger.ledger.storage.updateLinked

@Serializable
internal data class PoolTransaction(
    val transaction: Transaction,
    val confirmed: Boolean = true
) : ServiceClass, StorageAware<PoolTransaction> {
    @Transient
    override var id: StorageID? = null

    @Transient
    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(
            StoragePairs.Linked("transaction", TransactionStorageAdapter),
            StoragePairs.Native("confirmed")
        )

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        updateLinked(session, invalidated)

}