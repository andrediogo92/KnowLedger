package org.knowledger.ledger.storage.witness

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.storage.serial.WitnessSerializationStrategy

interface HashedWitness : Comparable<HashedWitness>,
                          Hashing, Witness {
    override fun compareTo(other: HashedWitness): Int =
        publicKey.compareTo(other.publicKey)

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.encodeToByteArray(WitnessSerializationStrategy, this as Witness)
}