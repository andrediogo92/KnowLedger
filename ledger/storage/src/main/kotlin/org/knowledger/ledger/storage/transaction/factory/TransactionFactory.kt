package org.knowledger.ledger.storage.transaction.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.PhysicalData
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.SignedTransaction
import org.knowledger.ledger.storage.transaction.Transaction
import java.security.PrivateKey
import java.security.PublicKey

interface TransactionFactory :
    CloningFactory<MutableTransaction> {
    fun create(
        privateKey: PrivateKey, publicKey: PublicKey,
        data: PhysicalData, hashers: Hashers,
        encoder: BinaryFormat, index: Int = -1
    ): MutableTransaction

    fun create(
        publicKey: PublicKey, data: PhysicalData,
        signature: EncodedSignature, hash: Hash,
        size: Int, index: Int = -1
    ): MutableTransaction

    fun create(
        publicKey: PublicKey, data: PhysicalData,
        signature: EncodedSignature, hasher: Hashers,
        encoder: BinaryFormat, index: Int = -1
    ): MutableTransaction

    fun create(
        transaction: SignedTransaction,
        hasher: Hashers, encoder: BinaryFormat, index: Int = -1
    ): MutableTransaction

    fun create(
        privateKey: PrivateKey, transaction: Transaction,
        hasher: Hashers, encoder: BinaryFormat, index: Int = -1
    ): MutableTransaction

    fun create(
        transaction: HashedTransaction
    ): MutableTransaction
}