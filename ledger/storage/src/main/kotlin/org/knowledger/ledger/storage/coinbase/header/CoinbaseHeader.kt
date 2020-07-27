package org.knowledger.ledger.storage.coinbase.header

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.HashSerializable
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.Payout
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
    val merkleRoot: Hash
    val payout: Payout

    // Difficulty is fixed at block generation time.
    val blockheight: Long
    val difficulty: Difficulty
    val extraNonce: Long

    val coinbaseParams: CoinbaseParams

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(CoinbaseHeaderSerializationStrategy, this)
}

