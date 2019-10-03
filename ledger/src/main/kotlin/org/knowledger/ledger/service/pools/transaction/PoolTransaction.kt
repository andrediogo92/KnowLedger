package org.knowledger.ledger.service.pools.transaction

import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.service.ServiceClass

@Serializable
data class PoolTransaction(
    val id: StorageID,
    val confirmed: Boolean = true
) : ServiceClass