package org.knowledger.ledger.service.pools.block

import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.service.ServiceClass

interface BlockPool : ServiceClass {
    val blocks: Set<StorageID>

    val firstUnconfirmed: StorageID?
        get() = blocks.firstOrNull()

    operator fun get(hash: StorageID): StorageID? =
        blocks.firstOrNull {
            it == hash
        }
}