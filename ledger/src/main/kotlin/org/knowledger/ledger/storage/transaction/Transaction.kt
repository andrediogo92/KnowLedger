package org.knowledger.ledger.storage.transaction

import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.storage.LedgerContract
import java.security.PublicKey

interface Transaction : HashSerializable,
                        LedgerContract,
                        Cloneable,
                        Comparable<Transaction> {
    // Agent's pub key.
    val publicKey: PublicKey
    val data: PhysicalData

    public override fun clone(): Transaction
}