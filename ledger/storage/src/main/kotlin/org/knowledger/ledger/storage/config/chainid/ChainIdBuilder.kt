@file:UseSerializers(HashSerializer::class)

package org.knowledger.ledger.storage.config.chainid

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.adapters.Tag
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.HashSerializable
import org.knowledger.ledger.storage.serial.ChainIdBuilderSerializationStrategy

internal data class ChainIdBuilder(
    val ledgerHash: Hash, val tag: Tag, val rawTag: Hash,
    val blockParams: BlockParams, val coinbaseParams: CoinbaseParams,
) : HashSerializable {
    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.encodeToByteArray(ChainIdBuilderSerializationStrategy, this)
}
