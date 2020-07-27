package org.knowledger.ledger.storage.coinbase.header

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.storage.serial.CoinbaseHeaderSerializationStrategy

interface HashedCoinbaseHeader : Hashing, CoinbaseHeader {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(CoinbaseHeaderSerializationStrategy, this as CoinbaseHeader)
}