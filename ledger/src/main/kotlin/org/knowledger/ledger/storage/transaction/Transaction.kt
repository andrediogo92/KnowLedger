package org.knowledger.ledger.storage.transaction

import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.core.serial.HashSerializable
import org.knowledger.ledger.core.storage.LedgerContract
import java.security.PublicKey

interface Transaction : HashSerializable,
                        LedgerContract,
                        Comparable<Transaction> {
    // Agent's pub key.
    val publicKey: PublicKey
    val data: PhysicalData


}