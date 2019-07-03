package pt.um.masb.ledger.service.transactions

import pt.um.masb.common.database.query.UnspecificQuery
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.Transaction
import pt.um.masb.ledger.storage.adapters.TransactionStorageAdapter
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