package org.knowledger.ledger.storage

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutputImpl
import java.security.PublicKey

internal fun transaction(
    publicKey: PublicKey, data: PhysicalData,
    signature: ByteArray, hash: Hash
): Transaction =
    HashedTransactionImpl(
        publicKey, data,
        signature, hash
    )

internal fun transactionOutput(
    publicKey: PublicKey, prevCoinbase: Hash,
    payout: Payout, txSet: MutableSet<Hash>,
    hash: Hash, hasher: Hashers, encoder: BinaryFormat
): TransactionOutput =
    HashedTransactionOutputImpl(
        publicKey, prevCoinbase, payout,
        txSet, hash, hasher, encoder
    )