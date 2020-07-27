package org.knowledger.ledger.storage.serial

import kotlinx.serialization.Encoder
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationStrategy
import org.knowledger.ledger.core.serial.DifficultySerializer
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.core.serial.compositeEncode
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.coinbase.header.CoinbaseHeader

internal object CoinbaseHeaderSerializationStrategy : SerializationStrategy<CoinbaseHeader>,
                                                      HashEncode {
    private val coinbaseParamsSerializer: SerializationStrategy<CoinbaseParams> =
        CoinbaseParamsSerializationStrategy
    private val difficultySerializer: SerializationStrategy<Difficulty>
        get() = DifficultySerializer
    private val payoutSerializer: SerializationStrategy<Payout>
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
                elementName = "merkleRoot", descriptor = hashDescriptor
            )
            element(
                elementName = "payout",
                descriptor = payoutSerializer.descriptor
            )
            element(
                elementName = blockheight.serialName, descriptor = blockheight
            )
            element(
                elementName = "difficulty",
                descriptor = difficultySerializer.descriptor
            )
            element(
                elementName = extraNonce.serialName, descriptor = extraNonce
            )
            element(
                elementName = "coinbaseParams",
                descriptor = coinbaseParamsSerializer.descriptor
            )
        }

    override fun serialize(encoder: Encoder, value: CoinbaseHeader) {
        compositeEncode(encoder) {
            encodeHash(0, value.merkleRoot)
            encodeSerializableElement(
                descriptor, 1, payoutSerializer, value.payout
            )
            encodeLongElement(descriptor, 2, value.blockheight)
            encodeSerializableElement(
                descriptor, 3, difficultySerializer, value.difficulty
            )
            encodeLongElement(descriptor, 4, value.extraNonce)
            encodeSerializableElement(
                descriptor, 5, coinbaseParamsSerializer, value.coinbaseParams
            )
        }
    }

}