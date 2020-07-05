package org.knowledger.ledger.storage.coinbase.header

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.serial.CoinbaseHeaderSerializationStrategy

/**
 * The coinbase transaction. Pays out to contributors to
 * the ledger.
 *
 * The coinbase will be continually updated to reflect
 * changes to the block.
 */
interface CoinbaseHeader : HashSerializable,
                           LedgerContract {
    val payout: Payout
    val coinbaseParams: CoinbaseParams
    val merkleRoot: Hash

    // Difficulty is fixed at block generation time.
    val difficulty: Difficulty
    val blockheight: Long
    val extraNonce: Long

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(CoinbaseHeaderSerializationStrategy, this)
}

