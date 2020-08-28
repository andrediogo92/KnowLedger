package org.knowledger.ledger.storage.coinbase.header

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.storage.serial.CoinbaseHeaderSerializationStrategy

interface HashedCoinbaseHeader : Hashing, CoinbaseHeader {
    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.encodeToByteArray(CoinbaseHeaderSerializationStrategy, this as CoinbaseHeader)
}