package org.knowledger.ledger.service.transactions

import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Transaction
import java.security.PublicKey


// ------------------------------
// Transactions over transactions.
// Go figure.
//
// Execution must be runtime determined.
// ------------------------------
internal fun QueryManager.getTransactionsFromAgent(
    tag: String, publicKey: PublicKey
): Outcome<Sequence<Transaction>, LoadFailure> =
    transactionStorageAdapter.let {
        queryResults(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id}
                    WHERE value.value.@class = :tag
                        AND publicKey = :publicKey
                """.trimIndent(),
                mapOf(
                    "tag" to tag,
                    "publicKey" to publicKey.encoded
                )
            ),
            it
        )
    }

internal fun QueryManager.getTransactionsFromAgent(
    tag: Hash, publicKey: PublicKey
): Outcome<Sequence<Transaction>, LoadFailure> =
    getTransactionsFromAgent(
        tag.base64Encoded(), publicKey
    )

internal fun QueryManager.getTransactionByIndex(
    blockHash: Hash, index: Int
): Outcome<Transaction, LoadFailure> =
    queryUniqueResult(
        UnspecificQuery(
            """
                SELECT expand(transactions[$index])
                FROM ${blockStorageAdapter.id}
                WHERE header.hash = :hash
            """.trimIndent(),
            mapOf(
                "hash" to blockHash.bytes
            )
        ), transactionStorageAdapter
    )

internal fun QueryManager.getTransactionByHash(
    tag: String, hash: Hash
): Outcome<Transaction, LoadFailure> =
    transactionStorageAdapter.let {
        queryUniqueResult(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id}
                    WHERE value.value.@class = :tag
                        AND hash = :hash
                """.trimIndent(),
                mapOf(
                    "tag" to tag,
                    "hash" to hash.bytes
                )
            ), it
        )
    }

internal fun QueryManager.getTransactionByHash(
    tag: Hash, hash: Hash
): Outcome<Transaction, LoadFailure> =
    getTransactionByHash(tag.base64Encoded(), hash)


//Execution must be runtime determined.
internal fun QueryManager.getTransactionsOrderedByTimestamp(
    tag: String
): Outcome<Sequence<Transaction>, LoadFailure> =
    transactionStorageAdapter.let {
        queryResults(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id}
                    WHERE value.value.@class = :tag
                    ORDER BY value.seconds ASC, value.nanos ASC
                """.trimIndent(),
                mapOf(
                    "tag" to tag
                )
            ),
            it
        )

    }

//Execution must be runtime determined.
internal fun QueryManager.getTransactionsOrderedByTimestamp(
    tag: Hash
): Outcome<Sequence<Transaction>, LoadFailure> =
    getTransactionsOrderedByTimestamp(tag.base64Encoded())


internal fun QueryManager.getTransactionsByClass(
    tag: String
): Outcome<Sequence<Transaction>, LoadFailure> =
    transactionStorageAdapter.let {
        queryResults(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${it.id}
                    WHERE value.value.@class = :tag
                """.trimIndent(),
                mapOf(
                    "tag" to tag
                )
            ),
            it
        )

    }

internal fun QueryManager.getTransactionsByClass(
    tag: Hash
): Outcome<Sequence<Transaction>, LoadFailure> =
    getTransactionsByClass(tag.base64Encoded())
