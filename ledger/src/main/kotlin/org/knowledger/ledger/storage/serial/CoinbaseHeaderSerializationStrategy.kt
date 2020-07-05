package org.knowledger.ledger.storage.serial

import kotlinx.serialization.Encoder
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationStrategy
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.serial.DifficultySerializer
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.serial.HashEncode
import org.knowledger.ledger.serial.compositeEncode
import org.knowledger.ledger.storage.coinbase.header.CoinbaseHeader

internal object CoinbaseHeaderSerializationStrategy : SerializationStrategy<CoinbaseHeader>,
                                                      HashEncode {
    val coinbaseParamsSerializer: SerializationStrategy<CoinbaseParams> =
        CoinbaseParams.serializer()
    val difficultySerializer: SerializationStrategy<Difficulty>
        get() = DifficultySerializer
    val payoutSerializer: SerializationStrategy<Payout>
        get() = PayoutSerializer

    override val descriptor: SerialDescriptor =
        SerialDescriptor("CoinbaseHeader") {
            val blockheight = PrimitiveDescriptor(
                "blockheight", PrimitiveKind.LONG
            )
            val extraNonce = PrimitiveDescriptor(
                "extraNonce", PrimitiveKind.LONG
            )
            element(
                elementName = "coinbaseParams",
                descriptor = coinbaseParamsSerializer.descriptor
            )
            element(
                elementName = "merkleRoot",
                descriptor = hashDescriptor
            )
            element(
                elementName = "payout",
                descriptor = payoutSerializer.descriptor
            )
            element(
                elementName = "difficulty",
                descriptor = difficultySerializer.descriptor
            )
            element(
                elementName = blockheight.serialName,
                descriptor = blockheight
            )
            element(
                elementName = extraNonce.serialName,
                descriptor = extraNonce
            )
        }

    override fun serialize(encoder: Encoder, value: CoinbaseHeader) {
        compositeEncode(encoder) {
            encodeSerializableElement(
                descriptor, 0,
                coinbaseParamsSerializer, value.coinbaseParams
            )
            encodeHash(1, value.merkleRoot)
            encodeSerializableElement(
                descriptor, 2, payoutSerializer, value.payout
            )
            encodeSerializableElement(
                descriptor, 3, difficultySerializer, value.difficulty
            )
            encodeLongElement(descriptor, 4, value.blockheight)
            encodeLongElement(descriptor, 5, value.extraNonce)
        }
    }

}