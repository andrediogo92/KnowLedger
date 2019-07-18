package org.knowledger.ledger.service.transactions

import org.knowledger.ledger.core.database.query.UnspecificQuery
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.adapters.TransactionStorageAdapter
import java.security.PublicKey


// ------------------------------
// Transactions over transactions.
// Go figure.
//
// Execution must be runtime determined.
// ------------------------------
internal fun PersistenceWrapper.getTransactionsFromAgent(
    chainHash: Hash,
    publicKey: PublicKey
): Outcome<Sequence<Transaction>, LoadFailure> =
    TransactionStorageAdapter.let {
        queryResults(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id}
                    WHERE chainId.hashId = :chainHash
                        AND publicKey = :publicKey
                """.trimIndent(),
                mapOf(
                    "chainHash" to chainHash.bytes,
                    "publicKey" to publicKey.encoded
                )
            ),
            it
        )
    }

internal fun PersistenceWrapper.getTransactionByHash(
    chainHash: Hash,
    hash: Hash
): Outcome<Transaction, LoadFailure> =
    TransactionStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id}
                    WHERE chainId.hashId = :chainHash
                        AND hashId = :hash
                """.trimIndent(),
                mapOf(
                    "chainHash" to chainHash.bytes,
                    "hash" to hash.bytes
                )
            ),
            it
        )

    }


//Execution must be runtime determined.
internal fun PersistenceWrapper.getTransactionsOrderedByTimestamp(
    chainHash: Hash
): Outcome<Sequence<Transaction>, LoadFailure> =
    TransactionStorageAdapter.let {
        queryResults(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id}
                    WHERE chainId.hashId = :chainHash
                    ORDER BY value.seconds DESC, value.nanos DESC
                """.trimIndent(),
                mapOf(
                    "chainHash" to chainHash.bytes
                )
            ),
            it
        )

    }

internal fun PersistenceWrapper.getTransactionsByClass(
    chainHash: Hash,
    typeName: String
): Outcome<Sequence<Transaction>, LoadFailure> =
    TransactionStorageAdapter.let {
        queryResults(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id}
                    WHERE chainId.hashId = :chainHash
                        AND value.value.@class = :typeName
                """.trimIndent(),
                mapOf(
                    "chainHash" to chainHash.bytes,
                    "typeName" to typeName
                )
            ),
            it
        )

    }