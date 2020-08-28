package org.knowledger.ledger.storage.transaction.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.core.calculateSizeAndHash
import org.knowledger.ledger.core.generateSignature
import org.knowledger.ledger.crypto.EncodedPrivateKey
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.PhysicalData
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.SignedTransaction
import org.knowledger.ledger.storage.transaction.SignedTransactionImpl
import org.knowledger.ledger.storage.transaction.TransactionImpl
import org.knowledger.ledger.storage.transaction.Transaction as RawTransaction

@OptIn(ExperimentalSerializationApi::class)
internal class HashedTransactionFactory : TransactionFactory {
    private fun generateHashAndSize(
        signedTransaction: SignedTransaction, hasher: Hashers, encoder: BinaryFormat,
    ): Pair<Int, Hash> =
        signedTransaction.calculateSizeAndHash(hasher, encoder)

    private fun generateHashAndSize(
        publicKey: EncodedPublicKey, data: PhysicalData,
        signature: EncodedSignature, hasher: Hashers, encoder: BinaryFormat,
    ): Pair<Int, Hash> = generateHashAndSize(
        SignedTransactionImpl(publicKey, data, signature), hasher, encoder
    )

    override fun create(
        privateKey: EncodedPrivateKey, publicKey: EncodedPublicKey,
        data: PhysicalData, hashers: Hashers, encoder: BinaryFormat, index: Int,
    ): HashedTransactionImpl =
        create(privateKey, TransactionImpl(publicKey, data), hashers, encoder, index)

    override fun create(
        publicKey: EncodedPublicKey, data: PhysicalData, signature: EncodedSignature,
        hasher: Hashers, encoder: BinaryFormat, index: Int,
    ): HashedTransactionImpl {
        val (size, hash) = generateHashAndSize(publicKey, data, signature, hasher, encoder)
        return create(publicKey, data, signature, hash, size, index)
    }

    override fun create(
        transaction: SignedTransaction, hasher: Hashers, encoder: BinaryFormat, index: Int,
    ): HashedTransactionImpl {
        val (size, hash) = generateHashAndSize(transaction, hasher, encoder)
        return with(transaction) { create(publicKey, data, signature, hash, size, index) }
    }

    override fun create(
        privateKey: EncodedPrivateKey, transaction: RawTransaction,
        hasher: Hashers, encoder: BinaryFormat, index: Int,
    ): HashedTransactionImpl {
        val signature = privateKey.generateSignature(transaction, encoder)
        return with(transaction) {
            create(SignedTransactionImpl(publicKey, data, signature), hasher, encoder, index)
        }
    }

    override fun create(
        publicKey: EncodedPublicKey, data: PhysicalData,
        signature: EncodedSignature, hash: Hash, size: Int, index: Int,
    ): HashedTransactionImpl =
        HashedTransactionImpl(publicKey, data, signature, hash, size, index)

    override fun create(transaction: Transaction): HashedTransactionImpl =
        with(transaction) { create(publicKey, data, signature, hash, approximateSize) }

    override fun create(other: MutableTransaction): HashedTransactionImpl =
        create(other as Transaction)
}