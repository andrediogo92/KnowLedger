package org.knowledger.ledger.serial.binary

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.serial.DifficultySerializer
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.serial.display.CoinbaseSerializer
import org.knowledger.ledger.serial.display.WitnessSerializer
import org.knowledger.ledger.serial.internal.AbstractCoinbaseSerializer
import org.knowledger.ledger.serial.internal.HashEncodeInBytes

internal object CoinbaseByteSerializer : AbstractCoinbaseSerializer(WitnessSerializer),
                                         HashEncodeInBytes {
    override fun CompositeEncoder.encodeDifficulty(
        index: Int, difficulty: Difficulty
    ) {
        encodeSerializableElement(
            CoinbaseSerializer.descriptor, index,
            DifficultySerializer, difficulty
        )
    }

    override fun CompositeDecoder.decodeDifficulty(
        index: Int
    ): Difficulty =
        decodeSerializableElement(
            CoinbaseSerializer.descriptor, index,
            DifficultySerializer
        )


    override fun CompositeEncoder.encodeCoinbaseParams(
        index: Int, params: CoinbaseParams
    ) {
        encodeSerializableElement(
            descriptor, index,
            CoinbaseParamsByteSerializer, params
        )
    }

    override fun CompositeDecoder.decodeCoinbaseParams(
        index: Int
    ): CoinbaseParams =
        decodeSerializableElement(
            descriptor, index,
            CoinbaseParamsByteSerializer
        )
}