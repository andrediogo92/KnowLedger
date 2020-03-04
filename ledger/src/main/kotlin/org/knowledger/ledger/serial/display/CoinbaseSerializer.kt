package org.knowledger.ledger.serial.display

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.SerialDescriptor
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.serial.internal.AbstractCoinbaseSerializer
import org.knowledger.ledger.serial.internal.HashEncodeForDisplay

internal object CoinbaseSerializer : AbstractCoinbaseSerializer(WitnessSerializer),
                                     HashEncodeForDisplay {
    override val difficultyDescriptor: SerialDescriptor
        get() = DifficultyDisplaySerializer.descriptor

    override fun CompositeEncoder.encodeDifficulty(
        index: Int, difficulty: Difficulty
    ) {
        encodeSerializableElement(
            descriptor, index,
            DifficultyDisplaySerializer, difficulty
        )
    }

    override fun CompositeDecoder.decodeDifficulty(
        index: Int
    ): Difficulty =
        decodeSerializableElement(
            descriptor, index,
            DifficultyDisplaySerializer
        )

    override val coinbaseParamsDescriptor: SerialDescriptor
        get() = CoinbaseParamsSerializer.descriptor

    override fun CompositeEncoder.encodeCoinbaseParams(
        index: Int, params: CoinbaseParams
    ) {
        encodeSerializableElement(
            descriptor, index,
            CoinbaseParamsSerializer, params
        )
    }

    override fun CompositeDecoder.decodeCoinbaseParams(
        index: Int
    ): CoinbaseParams =
        decodeSerializableElement(
            descriptor, index, CoinbaseParamsSerializer
        )
}