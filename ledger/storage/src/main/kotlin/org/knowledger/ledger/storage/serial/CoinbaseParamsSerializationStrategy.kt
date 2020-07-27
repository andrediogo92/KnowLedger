package org.knowledger.ledger.storage.serial

import kotlinx.serialization.Encoder
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationStrategy
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.config.coinbase.ImmutableCoinbaseParams
import org.knowledger.ledger.storage.immutableCopy

object CoinbaseParamsSerializationStrategy : SerializationStrategy<CoinbaseParams> {
    val serializer = ImmutableCoinbaseParams.serializer()
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: CoinbaseParams) =
        encoder.encodeSerializableValue(
            serializer, value as? ImmutableCoinbaseParams ?: value.immutableCopy()
        )
}