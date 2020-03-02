package org.knowledger.ledger.storage.witness

import kotlinx.serialization.Serializable
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.serial.display.WitnessSerializer

@Serializable(with = WitnessSerializer::class)
interface HashedWitness : Hashing,
                          Witness {
    override fun clone(): HashedWitness
}