package org.knowledger.ledger.storage.witness

import kotlinx.serialization.BinaryFormat
import org.knowledger.collections.SortedList
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.HashSerializable
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.serial.WitnessSerializationStrategy

interface Witness : HashSerializable, LedgerContract {

    val publicKey: EncodedPublicKey
    val previousWitnessIndex: Int
    val previousCoinbase: Hash
    val payout: Payout
    val transactionOutputs: SortedList<TransactionOutput>

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(WitnessSerializationStrategy, this)
}