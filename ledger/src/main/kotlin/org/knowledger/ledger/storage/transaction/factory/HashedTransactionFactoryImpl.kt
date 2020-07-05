package org.knowledger.ledger.storage.transaction.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.core.calculateSizeAndHash
import org.knowledger.ledger.core.generateSignature
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.SignedTransaction
import org.knowledger.ledger.storage.transaction.SignedTransactionImpl
import org.knowledger.ledger.storage.transaction.Transaction
import org.knowledger.ledger.storage.transaction.TransactionImpl
import java.security.PrivateKey
import java.security.PublicKey

internal object HashedTransactionFactoryImpl :
    HashedTransactionFactory {
    private fun generateHashAndSize(
        signedTransaction: SignedTransaction,
        hasher: Hashers, encoder: BinaryFormat
    ): Pair<Int, Hash> =
        signedTransaction.calculateSizeAndHash(
            hasher, encoder
        )

    private fun generateHashAndSize(
        publicKey: PublicKey, data: PhysicalData,
        signature: EncodedSignature,
        hasher: Hashers, encoder: BinaryFormat
    ): Pair<Int, Hash> =
        generateHashAndSize(
            SignedTransactionImpl(
                publicKey, data, signature
            ), hasher, encoder
        )

    override fun create(
        privateKey: PrivateKey, publicKey: PublicKey,
        data: PhysicalData, hashers: Hashers,
        encoder: BinaryFormat, index: Int
    ): HashedTransactionImpl =
        create(
            privateKey, TransactionImpl(
                publicKey, data
            ), hashers, encoder, index
        )

    override fun create(
        publicKey: PublicKey, data: PhysicalData,
        signature: EncodedSignature, hasher: Hashers,
        encoder: BinaryFormat, index: Int
    ): HashedTransactionImpl {
        val (size, hash) = generateHashAndSize(
            publicKey, data, signature, hasher, encoder
        )
        return create(
            publicKey, data, signature,
            hash, size, index
        )
    }

    override fun create(
        transaction: SignedTransaction, hasher: Hashers,
        encoder: BinaryFormat, index: Int
    ): HashedTransactionImpl {
        val (size, hash) = generateHashAndSize(
            transaction, hasher, encoder
        )
        return create(
            publicKey = transaction.publicKey,
            data = transaction.data,
            signature = transaction.signature,
            hash = hash, size = size, index = index
        )
    }

    override fun create(
        privateKey: PrivateKey, transaction: Transaction,
        hasher: Hashers, encoder: BinaryFormat, index: Int
    ): HashedTransactionImpl {
        val signature = privateKey.generateSignature(
            transaction, encoder
        )
        return create(
            SignedTransactionImpl(
                publicKey = transaction.publicKey,
                data = transaction.data,
                signature = signature
            ), hasher = hasher, encoder = encoder,
            index = index
        )
    }

    override fun create(
        publicKey: PublicKey, data: PhysicalData,
        signature: EncodedSignature, hash: Hash,
        size: Int, index: Int
    ): HashedTransactionImpl = HashedTransactionImpl(
        publicKey, data, signature,
        hash, size, index
    )

    override fun create(
        transaction: HashedTransaction
    ): HashedTransactionImpl = with(transaction) {
        create(
            publicKey, data,
            signature, hash,
            approximateSize
        )
    }

    override fun create(
        other: MutableTransaction
    ): HashedTransactionImpl = with(other) {
        create(
            publicKey, data,
            signature, hash,
            approximateSize
        )
    }
}