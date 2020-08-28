package org.knowledger.ledger.storage.transaction.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.crypto.EncodedPrivateKey
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.PhysicalData
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.transaction.SignedTransaction
import org.knowledger.ledger.storage.transaction.Transaction as RawTransaction

@OptIn(ExperimentalSerializationApi::class)
interface TransactionFactory : CloningFactory<MutableTransaction> {
    fun create(
        privateKey: EncodedPrivateKey, publicKey: EncodedPublicKey,
        data: PhysicalData, hashers: Hashers, encoder: BinaryFormat, index: Int = -1,
    ): MutableTransaction

    fun create(
        publicKey: EncodedPublicKey, data: PhysicalData,
        signature: EncodedSignature, hash: Hash, size: Int, index: Int = -1,
    ): MutableTransaction

    fun create(
        publicKey: EncodedPublicKey, data: PhysicalData,
        signature: EncodedSignature, hasher: Hashers, encoder: BinaryFormat, index: Int = -1,
    ): MutableTransaction

    fun create(
        transaction: SignedTransaction, hasher: Hashers, encoder: BinaryFormat, index: Int = -1,
    ): MutableTransaction

    fun create(
        privateKey: EncodedPrivateKey, transaction: RawTransaction,
        hasher: Hashers, encoder: BinaryFormat, index: Int = -1,
    ): MutableTransaction

    fun create(transaction: Transaction): MutableTransaction
}