package org.knowledger.ledger.storage.witness

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.storage.serial.WitnessSerializationStrategy

interface HashedWitness : Comparable<HashedWitness>,
                          Hashing, Witness {
    override fun compareTo(other: HashedWitness): Int =
        publicKey.compareTo(other.publicKey)

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(WitnessSerializationStrategy, this as Witness)
}